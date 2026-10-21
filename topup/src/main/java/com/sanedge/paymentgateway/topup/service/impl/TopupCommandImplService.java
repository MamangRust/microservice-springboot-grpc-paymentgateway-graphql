package com.sanedge.paymentgateway.topup.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.adapter.card.CardPort;
import com.sanedge.paymentgateway.common.adapter.model.Card;
import com.sanedge.paymentgateway.common.adapter.model.Saldo;
import com.sanedge.paymentgateway.common.adapter.saldo.SaldoPort;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.enums.Status;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.common.outbox.Outbox;
import com.sanedge.paymentgateway.common.outbox.OutboxEventFactory;
import com.sanedge.paymentgateway.common.outbox.OutboxRepository;
import com.sanedge.paymentgateway.topup.domain.requests.CreateTopupRequest;
import com.sanedge.paymentgateway.topup.domain.requests.UpdateTopupRequest;
import com.sanedge.paymentgateway.topup.domain.responses.TopupResponse;
import com.sanedge.paymentgateway.topup.domain.responses.TopupResponseDeleteAt;
import com.sanedge.paymentgateway.topup.models.Topup;
import com.sanedge.paymentgateway.topup.repository.TopupCommandRepository;
import com.sanedge.paymentgateway.topup.repository.TopupQueryRepository;
import com.sanedge.paymentgateway.topup.service.TopupCommandService;

import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.common.Attributes;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@RequiredArgsConstructor
@Service
public class TopupCommandImplService implements TopupCommandService {

    private static final String TOPIC_TOPUP_CREATE = "email-service-topic-topup-create";

    private final CardPort cardPort;
    private final SaldoPort saldoPort;
    private final TopupQueryRepository topupQueryRepository;
    private final TopupCommandRepository topupCommandRepository;
    private final Validator validator;
    private final TracingMetrics tracingMetrics;
    private final OutboxRepository outboxRepository;

    @Value("${notification.topup.email:admin@example.com}")
    private String notificationEmail;

    @Override
    public ApiResponse<TopupResponse> create(CreateTopupRequest req) {
        Attributes attrs = Attributes.builder()
                .put("cardNumber", req.getCardNumber() != null ? req.getCardNumber() : "")
                .build();

        return tracingMetrics.traceAndMeasure("createTopup", "create_topup", attrs, () -> {
            log.info("🚀 Starting CreateTopup: {}", req);

            try {
                validateRequest(req);

                if (!cardExists(req.getCardNumber())) {
                    log.error("❌ Card not found: {}", req.getCardNumber());
                    throw new ResourceNotFoundException("Card not found");
                }

                UUID topupNo = UUID.randomUUID();
                Topup topup = new Topup();
                topup.setTopupNo(topupNo);
                topup.setCardNumber(req.getCardNumber());
                topup.setTopupAmount(req.getTopupAmount());
                topup.setTopupMethod(req.getTopupMethod());
                topup.setStatus(Status.PENDING);
                topup.setTopupTime(Timestamp.valueOf(LocalDateTime.now()));
                topup.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                topup.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                Topup savedTopup = topupCommandRepository.save(topup);

                Saldo saldo = findSaldoByCardNumber(req.getCardNumber());
                if (saldo == null) {
                    log.error("❌ Saldo not found for card: {}", req.getCardNumber());
                    topupCommandRepository.updateTopupStatus(savedTopup.getTopupId(), "FAILED");
                    throw new ResourceNotFoundException("Saldo not found");
                }

                Long newBalance = (long) saldo.totalBalance() + req.getTopupAmount();
                saldoPort.updateSaldoBalance(new SaldoPort.BalanceUpdate(
                        saldo.cardNumber(), newBalance.intValue(), null, null, null, null, null));

                savedTopup.setStatus(Status.SUCCESS);
                savedTopup.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                topupCommandRepository.save(savedTopup);

                TopupResponse response = TopupResponse.from(savedTopup);

                log.info("✅ CreateTopup completed: card={} topup_amount={} new_balance={}",
                        req.getCardNumber(), req.getTopupAmount(), newBalance);

                enqueueTopupEmail(savedTopup, savedTopup.getTopupId());
                enqueueTopupStats(savedTopup, req.getCardNumber());

                return ApiResponse.<TopupResponse>builder()
                        .status("success")
                        .data(response)
                        .message("Topup created successfully for card=" + req.getCardNumber())
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to create topup for card={}", req.getCardNumber(), e);
                return ApiResponse.<TopupResponse>builder()
                        .status("error")
                        .data(null)
                        .message("Failed to create topup for card=" + req.getCardNumber())
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<TopupResponse> update(UpdateTopupRequest req) {
        Attributes attrs = Attributes.builder()
                .put("topupId", String.valueOf(req.getTopupId()))
                .put("cardNumber", req.getCardNumber() != null ? req.getCardNumber() : "")
                .build();

        return tracingMetrics.traceAndMeasure("updateTopup", "update_topup", attrs, () -> {
            log.info("🚀 Starting UpdateTopup: {}", req);

            try {
                validateRequest(req);

                Long topupId = req.getTopupId();
                if (topupId == null) {
                    log.error("topup_id is required");
                    return ApiResponse.<TopupResponse>builder().status("error").data(null).build();
                }

                if (!cardExists(req.getCardNumber())) {
                    log.error("❌ Card not found: {}", req.getCardNumber());
                    topupCommandRepository.updateTopupStatus(topupId, Status.FAILED.toString());
                    throw new ResourceNotFoundException("Card not found");
                }

                Topup existingTopup = topupQueryRepository.findById(topupId)
                        .orElseThrow(() -> {
                            log.error("❌ Topup {} not found", topupId);
                            topupCommandRepository.updateTopupStatus(topupId, Status.FAILED.toString());
                            return new ResourceNotFoundException("Topup not found");
                        });

                Long difference = req.getTopupAmount() - existingTopup.getTopupAmount();
                existingTopup.setTopupAmount(req.getTopupAmount());
                existingTopup.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                topupCommandRepository.save(existingTopup);

                Saldo saldo = findSaldoByCardNumber(req.getCardNumber());
                if (saldo == null) {
                    log.error("❌ Saldo not found for card: {}", req.getCardNumber());
                    topupCommandRepository.updateTopupStatus(topupId, Status.FAILED.toString());
                    throw new ResourceNotFoundException("Saldo not found");
                }

                Long newBalance = (long) saldo.totalBalance() + difference;
                saldoPort.updateSaldoBalance(new SaldoPort.BalanceUpdate(
                        saldo.cardNumber(), newBalance.intValue(), null, null, null, null, null));

                existingTopup.setStatus(Status.SUCCESS);
                existingTopup.setTopupMethod(req.getTopupMethod());
                existingTopup.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                topupCommandRepository.save(existingTopup);

                TopupResponse response = TopupResponse.from(existingTopup);
                log.info("✅ UpdateTopup completed: card={} topup_id={} new_amount={} new_balance={}",
                        req.getCardNumber(), topupId, req.getTopupAmount(), newBalance);

                return ApiResponse.<TopupResponse>builder()
                        .status("success")
                        .data(response)
                        .message("Topup updated successfully for card=" + req.getCardNumber())
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to update topup for card={}", req.getCardNumber(), e);
                return ApiResponse.<TopupResponse>builder()
                        .status("error")
                        .data(null)
                        .message("Failed to update topup for card=" + req.getCardNumber())
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<TopupResponseDeleteAt> trashed(Long topupId) {
        Attributes attrs = Attributes.builder()
                .put("topupId", String.valueOf(topupId))
                .build();

        return tracingMetrics.traceAndMeasure("trashTopup", "trash_topup", attrs, () -> {
            log.info("🗑️ Trashing topup id={}", topupId);
            try {
                Topup topup = topupCommandRepository.trashed(topupId);
                return ApiResponse.<TopupResponseDeleteAt>builder()
                        .status("success")
                        .data(TopupResponseDeleteAt.from(topup))
                        .message("Trashed topup id=" + topupId)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash topup id={}", topupId, e);
                return ApiResponse.<TopupResponseDeleteAt>builder()
                        .status("error")
                        .data(null)
                        .message("Failed to trash topup id=" + topupId)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<TopupResponseDeleteAt> restore(Long topupId) {
        Attributes attrs = Attributes.builder()
                .put("topupId", String.valueOf(topupId))
                .build();

        return tracingMetrics.traceAndMeasure("restoreTopup", "restore_topup", attrs, () -> {
            log.info("♻️ Restoring topup id={}", topupId);
            try {
                Topup topup = topupCommandRepository.restore(topupId);
                return ApiResponse.<TopupResponseDeleteAt>builder()
                        .status("success")
                        .data(TopupResponseDeleteAt.from(topup))
                        .message("Restored topup id=" + topupId)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore topup id={}", topupId, e);
                return ApiResponse.<TopupResponseDeleteAt>builder()
                        .status("error")
                        .data(null)
                        .message("Failed to restore topup id=" + topupId)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deletePermanent(Long topupId) {
        Attributes attrs = Attributes.builder()
                .put("topupId", String.valueOf(topupId))
                .build();

        return tracingMetrics.traceAndMeasure("deleteTopupPermanent", "delete_topup_permanent", attrs, () -> {
            log.info("🧨 Permanently deleting topup id={}", topupId);
            try {
                topupCommandRepository.deletePermanent(topupId);
                return ApiResponse.<Boolean>builder().status("success").data(true)
                        .message("Deleted topup id=" + topupId)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete topup id={}", topupId, e);
                return ApiResponse.<Boolean>builder().status("error").data(false)
                        .message("Failed to delete topup id=" + topupId).build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllTopups", "restore_all_topups", () -> {
            log.info("🔄 Restoring ALL trashed topups");
            try {
                topupCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder().status("success").data(true)
                        .message("Restored all trashed topups")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all topups", e);
                return ApiResponse.<Boolean>builder().status("error").data(false)
                        .message("Failed to restore all topups")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllTopups", "delete_all_topups", () -> {
            log.info("💣 Permanently deleting ALL trashed topups");
            try {
                topupCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder().status("success").data(true)
                        .message("Deleted all trashed topups")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all topups", e);
                return ApiResponse.<Boolean>builder().status("error").data(false)
                        .message("Failed to delete all topups")
                        .build();
            }
        });
    }

    /**
     * Enqueues a topup-success email to the configured notification address.
     *
     * <p>This port's {@code Card} carries no email, so the recipient is a
     * configured notification inbox rather than the cardholder — topic and
     * envelope still match the email-service contract.
     */
    /**
     * Enqueues a {@code stats.payment.topup.event} analytics event after the
     * topup row is persisted. Failure here must never roll back the business
     * write.
     */
    private void enqueueTopupStats(Topup topup, String cardNumber) {
        try {
            long cardId = 0L;
            try {
                Card card = cardPort.findCardByCardNumber(cardNumber);
                if (card != null) {
                    cardId = card.id();
                }
            } catch (RuntimeException e) {
                log.debug("Card lookup failed for topup stats: {}", e.getMessage());
            }
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("topup_id", topup.getTopupId());
            fields.put("topup_no", topup.getTopupNo() != null ? topup.getTopupNo().toString() : "");
            fields.put("card_number", topup.getCardNumber());
            fields.put("amount", (double) topup.getTopupAmount());
            fields.put("payment_method", topup.getTopupMethod());
            fields.put("status", topup.getStatus() != null ? topup.getStatus().toString() : "");
            // Compatibility fields read by stats-writer.
            fields.put("period", YearMonth.now().toString());
            fields.put("cardId", cardId);

            OutboxEventFactory.Envelope env = OutboxEventFactory.statsRich(
                    "stats.payment.topup.event", fields);
            Outbox outbox = new Outbox();
            outbox.setAggregateType("TOPUP");
            outbox.setAggregateId(String.valueOf(topup.getTopupId()));
            outbox.setTopic("stats.payment.topup.event");
            outbox.setPayload(env.payload());
            outboxRepository.enqueue(outbox);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue topup stats event: {}", e.getMessage());
        }
    }

    private void enqueueTopupEmail(Topup topup, Long topupId) {
        try {
            String subject = "Topup Successful - SanEdge";
            String body = String.format(
                    "Hello,\n\nYour topup of %d has been processed successfully.\n\nRegards,\nSupport Team",
                    topup.getTopupAmount());
            OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(
                    TOPIC_TOPUP_CREATE, notificationEmail, subject, body);
            Outbox event = new Outbox();
            event.setAggregateType("TOPUP");
            event.setAggregateId(String.valueOf(topupId));
            event.setTopic(TOPIC_TOPUP_CREATE);
            event.setPayload(envelope.payload());
            outboxRepository.enqueue(event);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue topup email: {}", e.getMessage());
        }
    }

    private boolean cardExists(String cardNumber) {
        try {
            cardPort.findCardByCardNumber(cardNumber);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    private Saldo findSaldoByCardNumber(String cardNumber) {
        try {
            return saldoPort.findByCardNumber(cardNumber);
        } catch (ResourceNotFoundException e) {
            return null;
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    private <T> void validateRequest(T req) {
        Set<ConstraintViolation<T>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }
            log.error("Validation failed: {}", sb.toString());
            throw new ConstraintViolationException("Validation failed", violations);
        }
    }
}
