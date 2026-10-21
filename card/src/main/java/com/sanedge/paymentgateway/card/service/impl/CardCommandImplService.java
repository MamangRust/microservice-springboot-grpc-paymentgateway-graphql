package com.sanedge.paymentgateway.card.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.card.domain.requests.CreateCardRequest;
import com.sanedge.paymentgateway.card.domain.requests.UpdateCardRequest;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.card.domain.responses.CardResponse;
import com.sanedge.paymentgateway.card.domain.responses.CardResponseDeleteAt;
import com.sanedge.paymentgateway.card.models.Card;
import com.sanedge.paymentgateway.card.repository.CardCommandRepository;
import com.sanedge.paymentgateway.card.service.CardCommandService;
import com.sanedge.paymentgateway.card.utils.CardNumberGenerator;
import com.sanedge.paymentgateway.common.adapter.user.UserPort;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.common.outbox.Outbox;
import com.sanedge.paymentgateway.common.outbox.OutboxEventFactory;
import com.sanedge.paymentgateway.common.outbox.OutboxRepository;

import io.opentelemetry.api.common.Attributes;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class CardCommandImplService implements CardCommandService {
    private final CardCommandRepository cardCommandRepository;
    private final UserPort userPort;
    private final Validator validator;
    private final TracingMetrics tracingMetrics;
    private final OutboxRepository outboxRepository;

    @Override
    public ApiResponse<CardResponse> createCard(CreateCardRequest req) {
        Attributes attrs = Attributes.builder()
                .put("user.id", String.valueOf(req.getUserId()))
                .build();

        return tracingMetrics.traceAndMeasure("createCard", "create_card", attrs, () -> {
            log.info("🆕 Creating card for user_id={}", req.getUserId());

            try {
                if (!validateRequest(req)) {
                    return ApiResponse.<CardResponse>builder()
                            .status("error")
                            .message("Validation failed")
                            .data(null)
                            .build();
                }

                assertUserExists(req.getUserId());

                Card card = new Card();
                try {
                    String cardNumber = CardNumberGenerator.randomCardNumber();
                    card.setCardNumber(cardNumber);
                } catch (Exception e) {
                    log.error("❌ Failed to generate card number", e);
                    return ApiResponse.<CardResponse>builder()
                            .status("error")
                            .message("Failed to generate card number")
                            .data(null)
                            .build();
                }

                card.setUserId(req.getUserId());
                card.setCardType(req.getCardType());
                card.setExpireDate(Date.valueOf(req.getExpireDate()));
                card.setCvv(req.getCvv());
                card.setCardProvider(req.getCardProvider());
                card.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                card.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                Card savedCard = cardCommandRepository.save(card);
                CardResponse response = CardResponse.from(savedCard);

                log.info("✅ Card created successfully with card_id={}", response.getId());

                enqueueCardStats(savedCard);

                return ApiResponse.<CardResponse>builder()
                        .status("success")
                        .message("Card created successfully")
                        .data(response)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to create card for user_id={}", req.getUserId(), e);
                return ApiResponse.<CardResponse>builder()
                        .status("error")
                        .message("Failed to create card")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<CardResponse> updateCard(UpdateCardRequest req) {
        Attributes attrs = Attributes.builder()
                .put("card.id", String.valueOf(req.getCardId()))
                .put("user.id", String.valueOf(req.getUserId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateCard", "update_card", attrs, () -> {
            log.info("🔄 Updating card id={} for user_id={}", req.getCardId(), req.getUserId());

            try {
                if (!validateRequest(req)) {
                    return ApiResponse.<CardResponse>builder()
                            .status("error")
                            .message("Validation failed")
                            .data(null)
                            .build();
                }

                if (req.getCardId() == null) {
                    log.error("card_id is required");
                    return ApiResponse.<CardResponse>builder()
                            .status("error")
                            .message("card_id is required")
                            .data(null)
                            .build();
                }

                assertUserExists(req.getUserId());

                Card card = cardCommandRepository.findById(req.getCardId()).orElseThrow(() -> {
                    log.error("💳 Card with id {} not found", req.getCardId());
                    return new IllegalArgumentException("Card not found");
                });

                card.setCardType(req.getCardType());
                card.setExpireDate(Date.valueOf(req.getExpireDate()));
                card.setCvv(req.getCvv());
                card.setCardProvider(req.getCardProvider());
                card.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                Card updatedCard = cardCommandRepository.save(card);
                CardResponse response = CardResponse.from(updatedCard);

                log.info("✅ Card updated successfully with card_id={}", response.getId());

                enqueueCardStats(updatedCard);

                return ApiResponse.<CardResponse>builder()
                        .status("success")
                        .message("Card updated successfully")
                        .data(response)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to update card id={} for user_id={}", req.getCardId(), req.getUserId(), e);
                return ApiResponse.<CardResponse>builder()
                        .status("error")
                        .message("Failed to update card")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<CardResponseDeleteAt> trashCard(Long id) {
        Attributes attrs = Attributes.builder()
                .put("card.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("trashCard", "trash_card", attrs, () -> {
            log.info("🗑️ Trashing card id={}", id);
            try {
                Card card = cardCommandRepository.trashed(id);
                return ApiResponse.<CardResponseDeleteAt>builder()
                        .status("success")
                        .message("Card trashed successfully")
                        .data(CardResponseDeleteAt.from(card))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash card id={}", id, e);
                return ApiResponse.<CardResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to trash card")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<CardResponseDeleteAt> restoreCard(Long id) {
        Attributes attrs = Attributes.builder()
                .put("card.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("restoreCard", "restore_card", attrs, () -> {
            log.info("♻️ Restoring card id={}", id);
            try {
                Card card = cardCommandRepository.restore(id);
                return ApiResponse.<CardResponseDeleteAt>builder()
                        .status("success")
                        .message("Card restored successfully")
                        .data(CardResponseDeleteAt.from(card))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore card id={}", id, e);
                return ApiResponse.<CardResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to restore card")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteCard(Long id) {
        Attributes attrs = Attributes.builder()
                .put("card.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("deleteCard", "delete_card", attrs, () -> {
            log.info("🧨 Permanently deleting card id={}", id);
            try {
                cardCommandRepository.deletePermanent(id);
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("Card permanently deleted")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete card id={}", id, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to permanently delete card")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllCards", "restore_all_cards", () -> {
            log.info("🔄 Restoring ALL trashed cards");
            try {
                cardCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("All cards restored successfully")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all cards", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to restore all cards")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllCards", "delete_all_cards", () -> {
            log.info("💣 Permanently deleting ALL trashed cards");
            try {
                cardCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("All cards permanently deleted")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all cards", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to delete all cards")
                        .data(false)
                        .build();
            }
        });
    }

    private void assertUserExists(Long userId) {
        // The user port throws ResourceNotFoundException when the user is missing.
        userPort.findById(userId == null ? 0 : userId.intValue());
    }

    /**
     * Enqueues a {@code stats.payment.card.event} analytics event after the card
     * row is persisted. Failure here must never roll back the business write,
     * hence the guarded try/catch.
     */
    private void enqueueCardStats(Card card) {
        try {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("card_id", card.getCardId());
            fields.put("user_id", card.getUserId());
            fields.put("card_number", card.getCardNumber());
            fields.put("card_type", card.getCardType());
            fields.put("card_provider", card.getCardProvider());
            // Compatibility field read by stats-writer.
            fields.put("cardId", card.getCardId());

            OutboxEventFactory.Envelope env = OutboxEventFactory.statsRich(
                    "stats.payment.card.event", fields);
            Outbox outbox = new Outbox();
            outbox.setAggregateType("CARD");
            outbox.setAggregateId(String.valueOf(card.getCardId()));
            outbox.setTopic("stats.payment.card.event");
            outbox.setPayload(env.payload());
            outboxRepository.enqueue(outbox);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue card stats event | cardId={}: {}", card.getCardId(), e.getMessage());
        }
    }

    private <T> boolean validateRequest(T req) {
        Set<ConstraintViolation<T>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage())
                        .append("; ");
            }
            log.error("Validation failed: {}", sb);
            return false;
        }
        return true;
    }
}
