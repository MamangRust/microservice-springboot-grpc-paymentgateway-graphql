package com.sanedge.paymentgateway.withdraw.service.impl;

import java.sql.Timestamp;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.adapter.card.CardPort;
import com.sanedge.paymentgateway.common.adapter.fraud.FraudFallback;
import com.sanedge.paymentgateway.common.adapter.fraud.FraudPort;
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
import com.sanedge.paymentgateway.pb.ai_security.SecurityDomain;
import com.sanedge.paymentgateway.withdraw.domain.requests.CreateWithdrawRequest;
import com.sanedge.paymentgateway.withdraw.domain.requests.UpdateWithdrawRequest;
import com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse;
import com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponseDeleteAt;
import com.sanedge.paymentgateway.withdraw.models.Withdraw;
import com.sanedge.paymentgateway.withdraw.repository.WithdrawCommandRepository;
import com.sanedge.paymentgateway.withdraw.repository.WithdrawQueryRepository;
import com.sanedge.paymentgateway.withdraw.service.WithdrawCommandService;

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
public class WithdrawCommandImplService implements WithdrawCommandService {

    private static final String TOPIC_WITHDRAW_CREATE = "email-service-topic-withdraw-create";

    private final WithdrawQueryRepository withdrawQueryRepository;
    private final WithdrawCommandRepository withdrawCommandRepository;
    private final CardPort cardPort;
    private final SaldoPort saldoPort;
    private final Validator validator;
    private final TracingMetrics tracingMetrics;
    private final OutboxRepository outboxRepository;
    private final FraudPort fraudPort;

    @Value("${notification.withdraw.email:admin@example.com}")
    private String notificationEmail;

    @Override
    public ApiResponse<WithdrawResponse> create(CreateWithdrawRequest req) {
        Attributes attrs = Attributes.builder()
                .put("cardNumber", req.getCardNumber() != null ? req.getCardNumber() : "")
                .build();

        return tracingMetrics.traceAndMeasure("createWithdraw", "create", attrs, () -> {
            try {
                validateRequest(req);

                if (req.getWithdrawAmount() == null || req.getWithdrawAmount() <= 0) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("withdraw_amount must be > 0")
                            .data(null)
                            .build();
                }

                Card card = findCard(req.getCardNumber());
                if (card == null) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("Card not found")
                            .data(null)
                            .build();
                }

                Saldo saldo = findSaldo(req.getCardNumber());
                if (saldo == null) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("Saldo not found")
                            .data(null)
                            .build();
                }

                if (saldo.totalBalance() < req.getWithdrawAmount()) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("Insufficient balance")
                            .data(null)
                            .build();
                }

                FraudPort.SecurityResult security = verifySecurity(card, req.getWithdrawAmount().doubleValue());

                if ("DENY".equals(security.action())) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("Withdraw denied by ai-security")
                            .data(null)
                            .build();
                }

                saldoPort.updateSaldoWithdraw(new SaldoPort.WithdrawUpdate(
                        saldo.cardNumber(), 0, null, req.getWithdrawAmount().intValue(), null, null, null));

                Withdraw withdraw = new Withdraw();
                withdraw.setCardNumber(req.getCardNumber());
                withdraw.setWithdrawNo(UUID.randomUUID());
                withdraw.setStatus(Status.PENDING);
                withdraw.setWithdrawTime((req.getWithdrawTime() != null ? Timestamp.valueOf(req.getWithdrawTime())
                        : new java.sql.Timestamp(System.currentTimeMillis())));
                withdraw.setWithdrawAmount(req.getWithdrawAmount());

                Withdraw created = withdrawCommandRepository.save(withdraw);

                Withdraw updated = withdrawCommandRepository.updateStatus(created.getWithdrawId(),
                        Status.SUCCESS.toString());

                enqueueWithdrawEmail(updated, updated.getWithdrawId());
                enqueueWithdrawStats(updated, card);

                return ApiResponse.<WithdrawResponse>builder()
                        .status("success")
                        .message("Created withdraw successfully")
                        .data(WithdrawResponse.from(updated))
                        .build();

            } catch (Exception e) {
                log.error("💥 Error creating withdraw", e);
                return ApiResponse.<WithdrawResponse>builder()
                        .status("error")
                        .message("Failed to create withdraw")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<WithdrawResponse> update(UpdateWithdrawRequest req) {
        Attributes attrs = Attributes.builder()
                .put("withdrawId", String.valueOf(req.getWithdrawId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateWithdraw", "update", attrs, () -> {
            try {
                validateRequest(req);

                if (req.getWithdrawId() == null) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("withdraw_id is required")
                            .data(null)
                            .build();
                }

                if (req.getWithdrawAmount() == null || req.getWithdrawAmount() <= 0) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("withdraw_amount must be > 0")
                            .data(null)
                            .build();
                }

                Card card = findCard(req.getCardNumber());
                if (card == null) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("Card not found")
                            .data(null)
                            .build();
                }

                var withdrawOpt = withdrawQueryRepository.findById(req.getWithdrawId());
                if (withdrawOpt.isEmpty()) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("Withdraw not found")
                            .data(null)
                            .build();
                }

                Saldo saldo = findSaldo(req.getCardNumber());
                if (saldo == null) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("Saldo not found")
                            .data(null)
                            .build();
                }

                if (saldo.totalBalance() < req.getWithdrawAmount()) {
                    return ApiResponse.<WithdrawResponse>builder()
                            .status("error")
                            .message("Insufficient balance")
                            .data(null)
                            .build();
                }

                saldoPort.updateSaldoWithdraw(new SaldoPort.WithdrawUpdate(
                        saldo.cardNumber(), 0, null, req.getWithdrawAmount().intValue(), null, null, null));

                var withdraw = withdrawOpt.get();
                withdraw.setCardNumber(req.getCardNumber());
                withdraw.setWithdrawAmount(req.getWithdrawAmount());
                withdraw.setWithdrawTime((req.getWithdrawTime() != null ? Timestamp.valueOf(req.getWithdrawTime())
                        : new java.sql.Timestamp(System.currentTimeMillis())));

                Withdraw updated = withdrawCommandRepository.save(withdraw);
                Withdraw refreshed = withdrawCommandRepository.updateStatus(updated.getWithdrawId(),
                        Status.SUCCESS.toString());

                return ApiResponse.<WithdrawResponse>builder()
                        .status("success")
                        .message("Updated withdraw successfully")
                        .data(WithdrawResponse.from(refreshed))
                        .build();

            } catch (Exception e) {
                log.error("💥 Error updating withdraw", e);
                return ApiResponse.<WithdrawResponse>builder()
                        .status("error")
                        .message("Failed to update withdraw")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<WithdrawResponseDeleteAt> trashed(Long withdrawId) {
        Attributes attrs = Attributes.builder()
                .put("withdrawId", String.valueOf(withdrawId))
                .build();

        return tracingMetrics.traceAndMeasure("trashedWithdraw", "trashed_withdraw", attrs, () -> {
            try {
                Withdraw withdraw = withdrawCommandRepository.trashed(withdrawId);
                return ApiResponse.<WithdrawResponseDeleteAt>builder()
                        .status("success")
                        .message("Withdraw trashed successfully!")
                        .data(WithdrawResponseDeleteAt.from(withdraw))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash withdraw id={}", withdrawId, e);
                return ApiResponse.<WithdrawResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to trash withdraw")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<WithdrawResponseDeleteAt> restore(Long withdrawId) {
        Attributes attrs = Attributes.builder()
                .put("withdrawId", String.valueOf(withdrawId))
                .build();

        return tracingMetrics.traceAndMeasure("restoreWithdraw", "restore_withdraw", attrs, () -> {
            try {
                Withdraw withdraw = withdrawCommandRepository.restore(withdrawId);
                return ApiResponse.<WithdrawResponseDeleteAt>builder()
                        .status("success")
                        .message("Withdraw restored successfully!")
                        .data(WithdrawResponseDeleteAt.from(withdraw))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore withdraw id={}", withdrawId, e);
                return ApiResponse.<WithdrawResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to restore withdraw")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deletePermanent(Long withdrawId) {
        Attributes attrs = Attributes.builder()
                .put("withdrawId", String.valueOf(withdrawId))
                .build();

        return tracingMetrics.traceAndMeasure("deletePermanentWithdraw", "delete_permanent_withdraw", attrs, () -> {
            try {
                withdrawCommandRepository.deletePermanent(withdrawId);
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("Withdraw permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete withdraw id={}", withdrawId, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to permanently delete withdraw")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllWithdraws", "restore_all_withdraws", () -> {
            try {
                withdrawCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("All withdraws restored successfully!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all withdraws", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to restore all withdraws")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllWithdraws", "delete_all_withdraws", () -> {
            try {
                withdrawCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("All withdraws permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all withdraws", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to delete all withdraws")
                        .data(false)
                        .build();
            }
        });
    }

    private Card findCard(String cardNumber) {
        try {
            return cardPort.findCardByCardNumber(cardNumber);
        } catch (ResourceNotFoundException e) {
            return null;
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    private Saldo findSaldo(String cardNumber) {
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

    /**
     * Runs the AI security check before any balance is moved. The ai-security
     * service is a soft dependency: when unreachable the call fails open
     * (FraudFallback) and the withdraw proceeds; only an explicit DENY verdict
     * from the service itself blocks the withdraw.
     */
    private FraudPort.SecurityResult verifySecurity(Card card, double amount) {
        return FraudFallback.verifySecurity(fraudPort, new FraudPort.SecurityQuery(
                SecurityDomain.WITHDRAW, String.valueOf(card.userId()), amount));
    }

    private <T> void validateRequest(T req) {
        Set<ConstraintViolation<T>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }
            throw new ConstraintViolationException("Validation failed: " + sb, violations);
        }
    }

    private void enqueueWithdrawStats(Withdraw withdraw, Card card) {
        try {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("withdraw_id", withdraw.getWithdrawId());
            fields.put("withdraw_no", withdraw.getWithdrawNo() != null ? withdraw.getWithdrawNo().toString() : "");
            fields.put("card_number", withdraw.getCardNumber());
            fields.put("amount", (double) withdraw.getWithdrawAmount());
            fields.put("status", withdraw.getStatus() != null ? withdraw.getStatus().toString() : "");
            // Compatibility fields read by stats-writer.
            fields.put("period", YearMonth.now().toString());
            fields.put("cardId", card != null ? (long) card.id() : 0L);

            OutboxEventFactory.Envelope env = OutboxEventFactory.statsRich(
                    "stats.payment.withdraw.event", fields);
            Outbox outbox = new Outbox();
            outbox.setAggregateType("WITHDRAW");
            outbox.setAggregateId(String.valueOf(withdraw.getWithdrawId()));
            outbox.setTopic("stats.payment.withdraw.event");
            outbox.setPayload(env.payload());
            outboxRepository.enqueue(outbox);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue withdraw stats event: {}", e.getMessage());
        }
    }

    private void enqueueWithdrawEmail(Withdraw withdraw, Long withdrawId) {
        try {
            String subject = "Withdraw Successful - SanEdge";
            String body = String.format(
                    "Hello,\n\nYour withdraw of %d has been processed successfully.\n\nRegards,\nSupport Team",
                    withdraw.getWithdrawAmount());
            OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(
                    TOPIC_WITHDRAW_CREATE, notificationEmail, subject, body);
            Outbox event = new Outbox();
            event.setAggregateType("WITHDRAW");
            event.setAggregateId(String.valueOf(withdrawId));
            event.setTopic(TOPIC_WITHDRAW_CREATE);
            event.setPayload(envelope.payload());
            outboxRepository.enqueue(event);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue withdraw email: {}", e.getMessage());
        }
    }
}
