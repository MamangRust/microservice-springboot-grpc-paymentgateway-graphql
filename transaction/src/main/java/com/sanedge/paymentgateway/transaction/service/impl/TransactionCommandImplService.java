package com.sanedge.paymentgateway.transaction.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.adapter.card.CardPort;
import com.sanedge.paymentgateway.common.adapter.fraud.FraudFallback;
import com.sanedge.paymentgateway.common.adapter.fraud.FraudPort;
import com.sanedge.paymentgateway.common.adapter.merchant.MerchantPort;
import com.sanedge.paymentgateway.common.adapter.model.Card;
import com.sanedge.paymentgateway.common.adapter.model.Merchant;
import com.sanedge.paymentgateway.common.adapter.saldo.SaldoPort;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.enums.Status;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.common.outbox.Outbox;
import com.sanedge.paymentgateway.common.outbox.OutboxEventFactory;
import com.sanedge.paymentgateway.common.outbox.OutboxRepository;
import com.sanedge.paymentgateway.transaction.domain.requests.CreateTransactionRequest;
import com.sanedge.paymentgateway.transaction.domain.requests.UpdateTransactionRequest;
import com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse;
import com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt;
import com.sanedge.paymentgateway.transaction.models.Transaction;
import com.sanedge.paymentgateway.transaction.repository.TransactionCommandRepository;
import com.sanedge.paymentgateway.transaction.repository.TransactionQueryRepository;
import com.sanedge.paymentgateway.transaction.service.TransactionCommandService;

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
public class TransactionCommandImplService implements TransactionCommandService {

    private static final String TOPIC_TRANSACTION_CREATE = "email-service-topic-transaction-create";

    private final TransactionQueryRepository transactionQueryRepository;
    private final TransactionCommandRepository transactionCommandRepository;
    private final Validator validator;
    private final TracingMetrics tracingMetrics;

    private final MerchantPort merchantPort;
    private final SaldoPort saldoPort;
    private final CardPort cardPort;
    private final OutboxRepository outboxRepository;

    private final FraudPort fraudPort;

    @Value("${notification.transaction.email:admin@example.com}")
    private String notificationEmail;

    @Override
    public ApiResponse<TransactionResponse> create(String apiKey, CreateTransactionRequest req) {
        Attributes attrs = Attributes.builder()
                .put("cardNumber", req.getCardNumber() != null ? req.getCardNumber() : "")
                .put("merchantId", String.valueOf(req.getMerchantId()))
                .put("amount", String.valueOf(req.getAmount()))
                .build();

        return tracingMetrics.traceAndMeasure("createTransaction", "create", attrs, () -> {
            log.info("▶️ Starting CreateTransaction process, apiKey={}, req={}", apiKey, req);

            validateRequest(req);

            try {
                Merchant merchant = findMerchantByApiKey(apiKey);

                Card card = findCardByCardNumber(req.getCardNumber());

                long saldoBalance = findSaldoBalance(req.getCardNumber(), "Saldo not found");

                if (saldoBalance < req.getAmount()) {
                    log.error("❌ Insufficient balance, requested: {}, available: {}", req.getAmount(),
                            saldoBalance);
                    throw new ResourceNotFoundException("Insufficient balance");
                }

                UUID transactionNo = UUID.randomUUID();

                checkFraud(transactionNo, card, req);

                long updatedSaldo = saldoBalance - req.getAmount();
                updateSaldoBalance(card.cardNumber(), updatedSaldo);

                Transaction transactionEntity = new Transaction();

                transactionEntity.setCardNumber(req.getCardNumber());
                transactionEntity.setMerchantId(req.getMerchantId());
                transactionEntity.setAmount(req.getAmount());
                transactionEntity.setPaymentMethod(req.getPaymentMethod());
                transactionEntity.setTransactionTime(Timestamp.valueOf(LocalDateTime.now()));
                transactionEntity.setTransactionNo(transactionNo);
                transactionEntity.setStatus(Status.PENDING);

                Transaction transaction;
                try {
                    transaction = transactionCommandRepository.save(transactionEntity);
                } catch (Exception e) {
                    log.error("❌ Failed to create transaction, rolling back saldo", e);

                    updateSaldoBalance(card.cardNumber(), saldoBalance);
                    throw new ResourceNotFoundException("Failed to create transaction");
                }

                transactionCommandRepository.updateTransactionStatus(transaction.getTransactionId(),
                        Status.SUCCESS.toString());

                Card merchantCard = findCardByUserId(merchant.userId());

                long merchantSaldo = findSaldoBalance(merchantCard.cardNumber(), "Merchant saldo not found");

                long updatedMerchantSaldo = merchantSaldo + req.getAmount();
                updateSaldoBalance(merchantCard.cardNumber(), updatedMerchantSaldo);

                TransactionResponse response = TransactionResponse.from(transaction);

                log.info("✅ CreateTransaction completed, transaction_id={}", response.getId());

                enqueueTransactionEmail(transaction, response.getId());
                enqueueTransactionStats(transaction, card, req.getMerchantId());

                return ApiResponse.<TransactionResponse>builder()
                        .status("success")
                        .message("Transaction created successfully")
                        .data(response)
                        .build();

            } catch (ResourceNotFoundException e) {
                log.error("🔥 Error in create transaction: {}", e.getMessage(), e);

                return ApiResponse.<TransactionResponse>builder()
                        .status("error")
                        .data(null)
                        .message("Error in create transaction")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<TransactionResponse> update(String apiKey, UpdateTransactionRequest req) {
        Attributes attrs = Attributes.builder()
                .put("transactionId", String.valueOf(req.getTransactionId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateTransaction", "update", attrs, () -> {
            log.info("▶️ Starting UpdateTransaction process: {}", req);

            validateRequest(req);

            Long transactionId = Optional.ofNullable(req.getTransactionId())
                    .orElseThrow(() -> new ResourceNotFoundException("transaction_id is required"));

            try {
                Transaction transaction = transactionQueryRepository.findById(transactionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Transaction " + transactionId + " not found"));

                Merchant merchant = findMerchantByApiKey(apiKey);

                if (transaction.getMerchantId().longValue() != merchant.id()) {
                    log.error("🚫 Unauthorized access to transaction {}", transactionId);
                    transactionCommandRepository.updateTransactionStatus(transactionId, Status.FAILED.toString());
                    throw new ResourceNotFoundException("unauthorized access");
                }

                Card card = findCardByCardNumber(transaction.getCardNumber());

                long saldoBalance = findSaldoBalance(card.cardNumber(), "Saldo not found");

                long restoredBalance = saldoBalance + transaction.getAmount();
                updateSaldoBalance(card.cardNumber(), restoredBalance);

                if (restoredBalance < req.getAmount()) {
                    log.error("❌ Insufficient balance after restore, available={}, requested={}",
                            restoredBalance, req.getAmount());
                    transactionCommandRepository.updateTransactionStatus(transactionId, Status.FAILED.toString());
                    throw new ResourceNotFoundException("Insufficient balance");
                }

                long updatedBalance = restoredBalance - req.getAmount();
                updateSaldoBalance(card.cardNumber(), updatedBalance);

                transaction.setAmount(req.getAmount());
                transaction.setPaymentMethod(req.getPaymentMethod());
                transaction.setTransactionTime((req.getTransactionTime() != null ? Timestamp.valueOf(req.getTransactionTime()) : new java.sql.Timestamp(System.currentTimeMillis())));

                Transaction updated = transactionCommandRepository.save(transaction);

                transactionCommandRepository.updateTransactionStatus(transactionId, Status.SUCCESS.toString());

                TransactionResponse response = TransactionResponse.from(updated);

                log.info("✅ Transaction {} updated successfully", transactionId);

                return ApiResponse.<TransactionResponse>builder()
                        .status("success")
                        .message("Transaction updated successfully")
                        .data(response)
                        .build();

            } catch (Exception e) {
                log.error("🔥 Error updating transaction: {}", e.getMessage(), e);
                return ApiResponse.<TransactionResponse>builder()
                        .status("error")
                        .message("Failed to update transaction")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<TransactionResponseDeleteAt> trashed(Long transactionId) {
        Attributes attrs = Attributes.builder()
                .put("transactionId", String.valueOf(transactionId))
                .build();

        return tracingMetrics.traceAndMeasure("trashTransaction", "trashed", attrs, () -> {
            log.info("🗑️ Trashing transaction id={}", transactionId);

            try {
                Transaction transaction = transactionCommandRepository.trashed(transactionId);
                return ApiResponse.<TransactionResponseDeleteAt>builder()
                        .status("success")
                        .message("🗑️ Transaction trashed successfully!")
                        .data(TransactionResponseDeleteAt.from(transaction))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash transaction id={}", transactionId, e);
                return ApiResponse.<TransactionResponseDeleteAt>builder()
                        .status("error")
                        .message("💥 Failed to trash transaction")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<TransactionResponseDeleteAt> restore(Long transactionId) {
        Attributes attrs = Attributes.builder()
                .put("transactionId", String.valueOf(transactionId))
                .build();

        return tracingMetrics.traceAndMeasure("restoreTransaction", "restore", attrs, () -> {
            log.info("♻️ Restoring transaction id={}", transactionId);

            try {
                Transaction transaction = transactionCommandRepository.restore(transactionId);
                return ApiResponse.<TransactionResponseDeleteAt>builder()
                        .status("success")
                        .message("♻️ Transaction restored successfully!")
                        .data(TransactionResponseDeleteAt.from(transaction))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore transaction id={}", transactionId, e);
                return ApiResponse.<TransactionResponseDeleteAt>builder()
                        .status("error")
                        .message("💥 Failed to restore transaction")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deletePermanent(Long transactionId) {
        Attributes attrs = Attributes.builder()
                .put("transactionId", String.valueOf(transactionId))
                .build();

        return tracingMetrics.traceAndMeasure("deletePermanentTransaction", "delete_permanent", attrs, () -> {
            log.info("🧨 Permanently deleting transaction id={}", transactionId);

            try {
                transactionCommandRepository.deletePermanent(transactionId);
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🧨 Transaction permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete transaction id={}", transactionId, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("💥 Failed to permanently delete transaction")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllTransactions", "restore_all", () -> {
            log.info("🔄 Restoring ALL trashed transactions");

            try {
                transactionCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🔄 All transactions restored successfully!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all transactions", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("💥 Failed to restore all transactions")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllTransactions", "delete_all", () -> {
            log.info("💣 Permanently deleting ALL trashed transactions");

            try {
                transactionCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("💣 All transactions permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all transactions", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("💥 Failed to delete all transactions")
                        .data(false)
                        .build();
            }
        });
    }

    private Merchant findMerchantByApiKey(String apiKey) {
        try {
            return merchantPort.findByApiKey(apiKey);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (StatusRuntimeException e) {
            throw mapNotFound(e, "Merchant not found");
        }
    }

    private Card findCardByCardNumber(String cardNumber) {
        try {
            return cardPort.findCardByCardNumber(cardNumber);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (StatusRuntimeException e) {
            throw mapNotFound(e, "Card not found");
        }
    }

    private Card findCardByUserId(int userId) {
        try {
            return cardPort.findCardByUserId(userId);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Merchant card not found");
        } catch (StatusRuntimeException e) {
            throw mapNotFound(e, "Merchant card not found");
        }
    }

    private long findSaldoBalance(String cardNumber, String notFoundMessage) {
        try {
            return saldoPort.findByCardNumber(cardNumber).totalBalance();
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(notFoundMessage);
        } catch (StatusRuntimeException e) {
            throw mapNotFound(e, notFoundMessage);
        }
    }

    private void updateSaldoBalance(String cardNumber, long newBalance) {
        saldoPort.updateSaldoBalance(new SaldoPort.BalanceUpdate(
                cardNumber, (int) newBalance, null, null, null, null, null));
    }

    private RuntimeException mapNotFound(StatusRuntimeException e, String message) {
        if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
            return new ResourceNotFoundException(message);
        }
        return e;
    }

    /**
     * Runs the AI fraud check before any balance is moved. The ai-security
     * service is a soft dependency: when it is unreachable the call fails open
     * (FraudFallback) and the transaction proceeds; only a positive fraud
     * verdict from the service itself blocks the transaction.
     */
    private void checkFraud(UUID transactionNo, Card card, CreateTransactionRequest req) {
        FraudPort.FraudResult result = FraudFallback.detectFraud(fraudPort, new FraudPort.FraudQuery(
                transactionNo.toString(),
                req.getMerchantId().intValue(),
                card.userId(),
                req.getAmount().doubleValue(),
                "IDR",
                req.getPaymentMethod() != null ? req.getPaymentMethod() : "",
                "",
                "",
                System.currentTimeMillis()));
        if (result.fraudulent()) {
            log.error("🚫 Fraud detected for transaction {}, reason={}, riskScore={}", transactionNo,
                    result.reason(), result.riskScore());
            throw new ResourceNotFoundException("Transaction flagged as fraudulent");
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
            throw new ConstraintViolationException("Validation failed: " + sb, violations);
        }
    }

    /**
     * Enqueues a {@code stats.payment.transaction.event} analytics event after
     * the transaction row is persisted. Failure here must never roll back the
     * business write, hence the guarded try/catch.
     */
    private void enqueueTransactionStats(Transaction transaction, Card card, long merchantId) {
        try {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("transaction_id", transaction.getTransactionId());
            fields.put("transaction_no",
                    transaction.getTransactionNo() != null ? transaction.getTransactionNo().toString() : "");
            fields.put("card_number", transaction.getCardNumber());
            fields.put("amount", (double) transaction.getAmount());
            fields.put("payment_method", transaction.getPaymentMethod());
            fields.put("merchant_id", merchantId);
            fields.put("status", transaction.getStatus() != null ? transaction.getStatus().toString() : "");
            // Compatibility fields read by stats-writer.
            fields.put("period", periodOf(transaction.getTransactionTime()));
            fields.put("cardId", card != null ? (long) card.id() : 0L);

            OutboxEventFactory.Envelope env = OutboxEventFactory.statsRich(
                    "stats.payment.transaction.event", fields);
            Outbox outbox = new Outbox();
            outbox.setAggregateType("TRANSACTION");
            outbox.setAggregateId(String.valueOf(transaction.getTransactionId()));
            outbox.setTopic("stats.payment.transaction.event");
            outbox.setPayload(env.payload());
            outboxRepository.enqueue(outbox);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue transaction stats event: {}", e.getMessage());
        }
    }

    private static String periodOf(Timestamp ts) {
        if (ts == null) {
            return YearMonth.now().toString();
        }
        return YearMonth.from(ts.toLocalDateTime()).toString();
    }

    private void enqueueTransactionEmail(Transaction transaction, Long transactionId) {
        try {
            String subject = "Transaction Successful - SanEdge";
            String body = String.format(
                    "Hello,\n\nYour transaction of %d has been processed successfully.\n\nRegards,\nSupport Team",
                    transaction.getAmount());
            OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(
                    TOPIC_TRANSACTION_CREATE, notificationEmail, subject, body);
            Outbox event = new Outbox();
            event.setAggregateType("TRANSACTION");
            event.setAggregateId(String.valueOf(transactionId));
            event.setTopic(TOPIC_TRANSACTION_CREATE);
            event.setPayload(envelope.payload());
            outboxRepository.enqueue(event);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue transaction email: {}", e.getMessage());
        }
    }
}
