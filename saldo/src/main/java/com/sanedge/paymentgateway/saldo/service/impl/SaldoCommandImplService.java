package com.sanedge.paymentgateway.saldo.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.adapter.card.CardPort;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.common.outbox.Outbox;
import com.sanedge.paymentgateway.common.outbox.OutboxEventFactory;
import com.sanedge.paymentgateway.common.outbox.OutboxRepository;
import com.sanedge.paymentgateway.saldo.domain.requests.CreateSaldoRequest;
import com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoBalance;
import com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoRequest;
import com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoWithdraw;
import com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse;
import com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt;
import com.sanedge.paymentgateway.saldo.models.Saldo;
import com.sanedge.paymentgateway.saldo.repository.SaldoCommandRepository;
import com.sanedge.paymentgateway.saldo.repository.SaldoQueryRepository;
import com.sanedge.paymentgateway.saldo.service.SaldoCommandService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@RequiredArgsConstructor
@Service
public class SaldoCommandImplService implements SaldoCommandService {

    private static final String TOPIC_SALDO_CREATE = "email-service-topic-saldo-create";

    private final CardPort cardPort;
    private final SaldoCommandRepository saldoCommandRepository;
    private final SaldoQueryRepository saldoQueryRepository;
    private final TracingMetrics tracingMetrics;
    private final OutboxRepository outboxRepository;

    @Value("${notification.saldo.email:admin@example.com}")
    private String notificationEmail;

    @Override
    public ApiResponse<SaldoResponse> create(CreateSaldoRequest request) {
        Attributes attrs = Attributes.builder()
                .put("cardNumber", request.getCardNumber() != null ? request.getCardNumber() : "")
                .build();

        return tracingMetrics.traceAndMeasure("createSaldo", "create_saldo", attrs, () -> {
            try {
                log.info("Creating saldo for card_number={}", request.getCardNumber());

                validateCardExists(request.getCardNumber());

                Saldo saldo = new Saldo();
                saldo.setCardNumber(request.getCardNumber());
                saldo.setTotalBalance(request.getTotalBalance());
                saldo.setWithdrawAmount(null);
                saldo.setWithdrawTime(null);

                Saldo savedSaldo = saldoCommandRepository.save(saldo);

                log.info("Saldo created successfully with id={} for card={}", savedSaldo.getSaldoId(),
                        request.getCardNumber());

                enqueueSaldoEmail(savedSaldo, savedSaldo.getSaldoId());
                enqueueSaldoStats(savedSaldo);

                return ApiResponse.<SaldoResponse>builder()
                        .status("success")
                        .message("Create saldo success")
                        .data(SaldoResponse.from(savedSaldo))
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to create saldo for card_number={}", request.getCardNumber(), e);
                return ApiResponse.<SaldoResponse>builder()
                        .status("error")
                        .message("Failed to create saldo")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<SaldoResponse> update(UpdateSaldoRequest request) {
        Attributes attrs = Attributes.builder()
                .put("saldo.id", String.valueOf(request.getSaldoId()))
                .put("cardNumber", request.getCardNumber() != null ? request.getCardNumber() : "")
                .build();

        return tracingMetrics.traceAndMeasure("updateSaldo", "update_saldo", attrs, () -> {
            try {
                log.info("Updating saldo id={} for card={}", request.getSaldoId(), request.getCardNumber());

                if (request.getSaldoId() == null) {
                    log.error("saldo_id is required");
                    throw new IllegalArgumentException("saldo_id is required");
                }

                validateCardExists(request.getCardNumber());

                Saldo saldo = saldoQueryRepository.findById(request.getSaldoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Saldo not found"));

                saldo.setCardNumber(request.getCardNumber());
                saldo.setTotalBalance(request.getTotalBalance());

                Saldo updateSaldo = saldoCommandRepository.save(saldo);

                log.info("Saldo updated successfully with id={} for card={}", updateSaldo.getSaldoId(),
                        request.getCardNumber());

                enqueueSaldoStats(updateSaldo);

                return ApiResponse.<SaldoResponse>builder()
                        .status("success")
                        .message("Update saldo success")
                        .data(SaldoResponse.from(updateSaldo))
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to update saldo id={} for card={}", request.getSaldoId(), request.getCardNumber(), e);
                return ApiResponse.<SaldoResponse>builder()
                        .status("error")
                        .message("Failed to update saldo")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<SaldoResponse> updateBalance(UpdateSaldoBalance request) {
        Attributes attrs = Attributes.builder()
                .put("cardNumber", request.getCardNumber() != null ? request.getCardNumber() : "")
                .build();

        return tracingMetrics.traceAndMeasure("updateSaldoBalance", "update_saldo_balance", attrs, () -> {
            try {
                log.info("Updating saldo balance for card={} new_balance={}", request.getCardNumber(),
                        request.getTotalBalance());

                Saldo saldo = saldoQueryRepository.findByCardNumber(request.getCardNumber())
                        .orElseThrow(() -> {
                            log.error("Saldo not found for card: {}", request.getCardNumber());
                            return new ResourceNotFoundException("Saldo not found");
                        });

                saldoCommandRepository.updateBalanceByCardNumber(request.getCardNumber(), request.getTotalBalance());
                saldo.setTotalBalance(request.getTotalBalance());

                log.info("Saldo balance updated for card={} new_balance={}", request.getCardNumber(),
                        request.getTotalBalance());

                enqueueSaldoStats(saldo);

                return ApiResponse.<SaldoResponse>builder()
                        .status("success")
                        .message("Update saldo balance success")
                        .data(SaldoResponse.from(saldo))
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to update saldo balance for card={}", request.getCardNumber(), e);
                return ApiResponse.<SaldoResponse>builder()
                        .status("error")
                        .message("Failed to update saldo balance")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<SaldoResponse> updateWithdraw(UpdateSaldoWithdraw request) {
        Attributes attrs = Attributes.builder()
                .put("cardNumber", request.getCardNumber() != null ? request.getCardNumber() : "")
                .build();

        return tracingMetrics.traceAndMeasure("updateSaldoWithdraw", "update_saldo_withdraw", attrs, () -> {
            try {
                log.info("Updating saldo withdraw for card={} withdraw_amount={}", request.getCardNumber(),
                        request.getWithdrawAmount());

                Saldo saldo = saldoQueryRepository.findByCardNumber(request.getCardNumber())
                        .orElseThrow(() -> {
                            log.error("Saldo not found for card: {}", request.getCardNumber());
                            return new ResourceNotFoundException("Saldo not found");
                        });

                saldoCommandRepository.updateWithdrawByCardNumber(request.getCardNumber(), request.getWithdrawAmount());
                saldo.setWithdrawAmount(request.getWithdrawAmount());
                if (request.getWithdrawTime() != null) {
                    saldo.setWithdrawTime(java.sql.Timestamp.valueOf(request.getWithdrawTime()));
                }

                log.info("Saldo withdraw updated for card={} withdraw_amount={}", request.getCardNumber(),
                        request.getWithdrawAmount());

                enqueueSaldoStats(saldo);

                return ApiResponse.<SaldoResponse>builder()
                        .status("success")
                        .message("Update saldo withdraw success")
                        .data(SaldoResponse.from(saldo))
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to update saldo withdraw for card={}", request.getCardNumber(), e);
                return ApiResponse.<SaldoResponse>builder()
                        .status("error")
                        .message("Failed to update saldo withdraw")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<SaldoResponseDeleteAt> trash(Long id) {
        Attributes attrs = Attributes.builder()
                .put("saldo.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("trashSaldo", "trash_saldo", attrs, () -> {
            try {
                log.info("🗑️ Trashing saldo id={}", id);
                Saldo saldo = saldoCommandRepository.trashed(id);
                return ApiResponse.<SaldoResponseDeleteAt>builder()
                        .status("success")
                        .message("Trash saldo success")
                        .data(SaldoResponseDeleteAt.from(saldo))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash saldo id={}", id, e);
                return ApiResponse.<SaldoResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to trash saldo")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<SaldoResponseDeleteAt> restore(Long id) {
        Attributes attrs = Attributes.builder()
                .put("saldo.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("restoreSaldo", "restore_saldo", attrs, () -> {
            try {
                log.info("♻️ Restoring saldo id={}", id);
                Saldo saldo = saldoCommandRepository.restore(id);
                return ApiResponse.<SaldoResponseDeleteAt>builder()
                        .status("success")
                        .message("Restore saldo success")
                        .data(SaldoResponseDeleteAt.from(saldo))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore saldo id={}", id, e);
                return ApiResponse.<SaldoResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to restore saldo")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> delete(Long id) {
        Attributes attrs = Attributes.builder()
                .put("saldo.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("deleteSaldo", "delete_saldo", attrs, () -> {
            try {
                log.info("🧨 Permanently deleting saldo id={}", id);
                saldoCommandRepository.deletePermanent(id);
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("Delete saldo success")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete saldo id={}", id, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to delete saldo")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllSaldos", "restore_all_saldos", () -> {
            try {
                log.info("🔄 Restoring ALL trashed saldos");
                saldoCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("Restore all saldo success")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all saldos", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to restore all saldo")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllSaldos", "delete_all_saldos", () -> {
            try {
                log.info("💣 Permanently deleting ALL trashed saldos");
                saldoCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("Delete all saldo success")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all saldos", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to delete all saldo")
                        .data(false)
                        .build();
            }
        });
    }

    /**
     * Verifies that a card exists by calling the remote card service. The
     * monolith validated the card through {@code cardQueryRepository}; in the
     * modular setup that lookup becomes an outbound gRPC call.
     */
    private void validateCardExists(String cardNumber) {
        // The card port throws ResourceNotFoundException when the card is missing.
        cardPort.findCardByCardNumber(cardNumber);
    }

    /**
     * Enqueues a {@code stats.payment.saldo.event} analytics event after the
     * saldo row is persisted. Failure here must never roll back the business
     * write, hence the guarded try/catch.
     */
    private void enqueueSaldoStats(Saldo saldo) {
        try {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("card_number", saldo.getCardNumber());
            fields.put("total_balance", saldo.getTotalBalance());
            // Compatibility fields read by stats-writer.
            fields.put("amount", saldo.getTotalBalance() != null ? saldo.getTotalBalance().doubleValue() : 0d);

            OutboxEventFactory.Envelope env = OutboxEventFactory.statsRich(
                    "stats.payment.saldo.event", fields);
            Outbox outbox = new Outbox();
            outbox.setAggregateType("SALDO");
            outbox.setAggregateId(String.valueOf(saldo.getSaldoId()));
            outbox.setTopic("stats.payment.saldo.event");
            outbox.setPayload(env.payload());
            outboxRepository.enqueue(outbox);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue saldo stats event: {}", e.getMessage());
        }
    }

    /**
     * Enqueues a saldo-create email to the configured notification address.
     *
     * <p>The blueprint never produced this topic; this producer is new and follows
     * the shared envelope contract. Recipient is a configured notification inbox.
     */
    private void enqueueSaldoEmail(Saldo saldo, Long saldoId) {
        try {
            String subject = "Saldo Created - SanEdge";
            String body = String.format(
                    "Hello,\n\nA new saldo has been created for your card with initial balance %d.\n\nRegards,\nSupport Team",
                    saldo.getTotalBalance());
            OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(
                    TOPIC_SALDO_CREATE, notificationEmail, subject, body);
            Outbox event = new Outbox();
            event.setAggregateType("SALDO");
            event.setAggregateId(String.valueOf(saldoId));
            event.setTopic(TOPIC_SALDO_CREATE);
            event.setPayload(envelope.payload());
            outboxRepository.enqueue(event);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue saldo email: {}", e.getMessage());
        }
    }
}
