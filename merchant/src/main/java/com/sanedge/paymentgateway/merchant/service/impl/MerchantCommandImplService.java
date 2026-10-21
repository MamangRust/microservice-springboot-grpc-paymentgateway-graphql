package com.sanedge.paymentgateway.merchant.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.merchant.domain.requests.CreateMerchantRequest;
import com.sanedge.paymentgateway.merchant.domain.requests.UpdateMerchantRequest;
import com.sanedge.paymentgateway.common.adapter.user.UserPort;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt;
import com.sanedge.paymentgateway.common.enums.Status;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.common.adapter.model.User;
import com.sanedge.paymentgateway.common.outbox.Outbox;
import com.sanedge.paymentgateway.common.outbox.OutboxEventFactory;
import com.sanedge.paymentgateway.common.outbox.OutboxRepository;
import com.sanedge.paymentgateway.merchant.models.Merchant;
import com.sanedge.paymentgateway.merchant.repository.MerchantCommandRepository;
import com.sanedge.paymentgateway.merchant.repository.MerchantQueryRepository;
import com.sanedge.paymentgateway.merchant.service.MerchantCommandService;
import com.sanedge.paymentgateway.merchant.utils.ApiKeyGenerator;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantCommandImplService implements MerchantCommandService {

    private static final String TOPIC_MERCHANT_CREATE = "email-service-topic-merchant-create";
    private static final String TOPIC_MERCHANT_UPDATE_STATUS = "email-service-topic-merchant-update-status";

    private final UserPort userPort;
    private final MerchantQueryRepository merchantQueryRepository;
    private final MerchantCommandRepository merchantCommandRepository;
    private final TracingMetrics tracingMetrics;
    private final OutboxRepository outboxRepository;

    @Override
    public ApiResponse<MerchantResponse> createMerchant(CreateMerchantRequest req) {
        Attributes attrs = Attributes.builder()
                .put("operation", "create_merchant")
                .put("service.name", "merchant-command-service")
                .put("merchant.name", req.getName() != null ? req.getName() : "")
                .build();

        return tracingMetrics.traceAndMeasure("createMerchant", "create_merchant", attrs, () -> {
            log.info("🏪 Creating merchant | Name: {}, UserId: {}", req.getName(), req.getUserId());

            try {
                validateUserExists(req.getUserId());

                boolean merchantExists = merchantQueryRepository.existsByName(req.getName());
                if (merchantExists) {
                    log.error("❌ Merchant name already taken | Name: {}", req.getName());
                    throw new IllegalArgumentException("Merchant name already taken");
                }

                String apiKey = ApiKeyGenerator.generateApiKey();
                UUID merchantNo = UUID.randomUUID();

                Merchant merchant = new Merchant();
                merchant.setName(req.getName());
                merchant.setMerchantNo(merchantNo);
                merchant.setUserId(req.getUserId());
                merchant.setApiKey(apiKey);
                merchant.setStatus(Status.PENDING);
                merchant.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

                Merchant savedMerchant = merchantCommandRepository.save(merchant);

                log.info("✅ Merchant created successfully | Id: {}, ApiKey: {}", savedMerchant.getMerchantId(), apiKey);

                sendMerchantEmail(savedMerchant, TOPIC_MERCHANT_CREATE, "Merchant Registered Successfully",
                        String.format(
                                "Hello,\n\nYour merchant account '%s' has been created successfully.\n\nRegards,\nSupport Team",
                                savedMerchant.getName()));

                enqueueMerchantStats(savedMerchant);

                return ApiResponse.<MerchantResponse>builder()
                        .status("success")
                        .data(MerchantResponse.from(savedMerchant))
                        .message("Merchant created successfully")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to create merchant | Name: {}, UserId: {}", req.getName(), req.getUserId(), e);
                return ApiResponse.<MerchantResponse>builder()
                        .status("error")
                        .data(null)
                        .message("Failed to create merchant")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<MerchantResponse> updateMerchant(UpdateMerchantRequest req) {
        Attributes attrs = Attributes.builder()
                .put("operation", "update_merchant")
                .put("service.name", "merchant-command-service")
                .put("merchant.id", String.valueOf(req.getMerchantId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateMerchant", "update_merchant", attrs, () -> {
            log.info("🛠️ Updating merchant | Id: {}", req.getMerchantId());

            try {
                Merchant merchant = merchantQueryRepository.findById(req.getMerchantId())
                        .orElseThrow(() -> {
                            log.error("❌ Merchant not found with id {}", req.getMerchantId());
                            return new ResourceNotFoundException("Merchant not found");
                        });

                merchant.setName(req.getName());
                merchant.setStatus(Status.valueOf(req.getStatus().toUpperCase()));
                merchant.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                if (req.getUserId() != null) {
                    validateUserExists(req.getUserId());
                    merchant.setUserId(req.getUserId());
                }

                Merchant updatedMerchant = merchantCommandRepository.save(merchant);

                log.info("✅ Merchant updated successfully | Id: {}", updatedMerchant.getMerchantId());

                enqueueMerchantStats(updatedMerchant);

                return ApiResponse.<MerchantResponse>builder()
                        .status("success")
                        .data(MerchantResponse.from(updatedMerchant))
                        .message("Merchant updated successfully")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to update merchant | Id: {}", req.getMerchantId(), e);
                return ApiResponse.<MerchantResponse>builder()
                        .status("error")
                        .data(null)
                        .message("Failed to update merchant")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<MerchantResponseDeleteAt> trashMerchant(Long id) {
        Attributes attrs = Attributes.builder()
                .put("operation", "trash_merchant")
                .put("service.name", "merchant-command-service")
                .put("merchant.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("trashMerchant", "trash_merchant", attrs, () -> {
            log.info("🗑️ Trashing merchant id={}", id);
            try {
                Merchant merchant = merchantCommandRepository.trashed(id);
                return ApiResponse.<MerchantResponseDeleteAt>builder()
                        .status("success")
                        .data(MerchantResponseDeleteAt.from(merchant))
                        .message("Merchant trashed successfully")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash merchant id={}", id, e);
                return ApiResponse.<MerchantResponseDeleteAt>builder()
                        .status("error")
                        .data(null)
                        .message("Merchant trashing failed")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<MerchantResponseDeleteAt> restoreMerchant(Long id) {
        Attributes attrs = Attributes.builder()
                .put("operation", "restore_merchant")
                .put("service.name", "merchant-command-service")
                .put("merchant.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("restoreMerchant", "restore_merchant", attrs, () -> {
            log.info("♻️ Restoring merchant id={}", id);
            try {
                Merchant merchant = merchantCommandRepository.restore(id);
                return ApiResponse.<MerchantResponseDeleteAt>builder()
                        .status("success")
                        .data(MerchantResponseDeleteAt.from(merchant))
                        .message("Merchant restored successfully")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore merchant id={}", id, e);
                return ApiResponse.<MerchantResponseDeleteAt>builder()
                        .status("error")
                        .data(null)
                        .message("Merchant restoring failed")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteMerchant(Long id) {
        Attributes attrs = Attributes.builder()
                .put("operation", "delete_merchant")
                .put("service.name", "merchant-command-service")
                .put("merchant.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("deleteMerchant", "delete_merchant", attrs, () -> {
            log.info("🧨 Permanently deleting merchant id={}", id);
            try {
                merchantCommandRepository.deletePermanent(id);
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .data(true)
                        .message("Merchant permanently deleted")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete merchant id={}", id, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .data(false)
                        .message("Merchant deletion failed")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        Attributes attrs = Attributes.builder()
                .put("operation", "restore_all_merchants")
                .put("service.name", "merchant-command-service")
                .build();

        return tracingMetrics.traceAndMeasure("restoreAllMerchants", "restore_all_merchants", attrs, () -> {
            log.info("🔄 Restoring ALL trashed merchants");
            try {
                merchantCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .data(true)
                        .message("Restored all trashed merchants")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all merchants", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .data(false)
                        .message("Failed to restore all merchants")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        Attributes attrs = Attributes.builder()
                .put("operation", "delete_all_merchants")
                .put("service.name", "merchant-command-service")
                .build();

        return tracingMetrics.traceAndMeasure("deleteAllMerchants", "delete_all_merchants", attrs, () -> {
            log.info("💣 Permanently deleting ALL trashed merchants");
            try {
                merchantCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .data(true)
                        .message("Deleted all trashed merchants")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all merchants", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .data(false)
                        .message("Failed to delete all merchants")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<MerchantResponse> updateMerchantStatus(Integer merchantId, String status) {
        Attributes attrs = Attributes.builder()
                .put("merchant.id", String.valueOf(merchantId))
                .build();

        return tracingMetrics.traceAndMeasure("updateMerchantStatus", "update_merchant_status", attrs, () -> {
            log.info("✏️ Updating merchant status | Id: {}", merchantId);
            try {
                Merchant merchant = merchantQueryRepository.findMerchantById(merchantId.longValue())
                        .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

                try {
                    merchant.setStatus(Status.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    merchant.setStatus(Status.PENDING);
                }

                Merchant updated = merchantCommandRepository.save(merchant);

                sendMerchantEmail(updated, TOPIC_MERCHANT_UPDATE_STATUS, "Merchant Status Updated",
                        String.format(
                                "Hello,\n\nYour merchant account '%s' status has changed. New status: %s.\n\nRegards,\nSupport Team",
                                updated.getName(), updated.getStatus()));

                return ApiResponse.<MerchantResponse>builder()
                        .status("success")
                        .data(MerchantResponse.from(updated))
                        .message("Merchant status updated successfully")
                        .build();
            } catch (ResourceNotFoundException ex) {
                log.warn("❌ Resource not found: {}", ex.getMessage());
                return ApiResponse.<MerchantResponse>builder()
                        .status("error")
                        .message("Resource not found")
                        .data(null)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to update merchant status | Id: {}", merchantId, e);
                return ApiResponse.<MerchantResponse>builder()
                        .status("error")
                        .message("Failed to update merchant status")
                        .data(null)
                        .build();
            }
        });
    }

    private void validateUserExists(Long userId) {
        // The user port throws ResourceNotFoundException when the user is missing.
        userPort.findById(userId == null ? 0 : userId.intValue());
    }

    /**
     * Enqueues a merchant email to the owner's address.
     *
     * <p>Best-effort: a failed recipient lookup is logged and skipped so it never
     * aborts the merchant write.
     */
    private void sendMerchantEmail(Merchant merchant, String topic, String subject, String body) {
        try {
            User owner = userPort.findById(merchant.getUserId().intValue());
            OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(topic, owner.email(), subject, body);

            Outbox event = new Outbox();
            event.setAggregateType("MERCHANT");
            event.setAggregateId(String.valueOf(merchant.getMerchantId()));
            event.setTopic(topic);
            event.setPayload(envelope.payload());
            outboxRepository.enqueue(event);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue merchant email | topic={} merchantId={}: {}",
                    topic, merchant.getMerchantId(), e.getMessage());
        }
    }

    /**
     * Enqueues a {@code stats.payment.merchant.event} analytics event after the
     * merchant row is persisted. Failure here must never roll back the business
     * write, hence the guarded try/catch.
     */
    private void enqueueMerchantStats(Merchant merchant) {
        try {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("merchant_id", merchant.getMerchantId());
            fields.put("user_id", merchant.getUserId());
            fields.put("name", merchant.getName());
            fields.put("status", merchant.getStatus() != null ? merchant.getStatus().toString() : "");
            // Compatibility field read by stats-writer.
            fields.put("merchantId", merchant.getMerchantId());

            OutboxEventFactory.Envelope env = OutboxEventFactory.statsRich(
                    "stats.payment.merchant.event", fields);
            Outbox outbox = new Outbox();
            outbox.setAggregateType("MERCHANT");
            outbox.setAggregateId(String.valueOf(merchant.getMerchantId()));
            outbox.setTopic("stats.payment.merchant.event");
            outbox.setPayload(env.payload());
            outboxRepository.enqueue(outbox);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue merchant stats event | merchantId={}: {}",
                    merchant.getMerchantId(), e.getMessage());
        }
    }
}
