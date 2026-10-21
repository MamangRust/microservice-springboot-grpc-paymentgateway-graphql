package com.sanedge.paymentgateway.statswriter.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes {@code stats.*.event} topics and writes each event into ClickHouse.
 *
 * <p>Manual-ack: a poison message is acknowledged (not redelivered) after it is
 * logged, so a single bad payload cannot stall the partition.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.backfill.enabled", havingValue = "false", matchIfMissing = true)
public class StatsWriterConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StatsClickHouseWriter writer;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            writer.ensureTable();
        } catch (Exception e) {
            log.error("failed to ensure ClickHouse stats_volume table: {}", e.getMessage());
        }
    }

    @KafkaListener(
            topicPattern = "stats\\..*\\.event",
            containerFactory = "statsListenerContainerFactory",
            groupId = "${app.kafka.stats.group-id:stats-writer-group}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(record.value());
            String period = node.path("period").asText(node.path("createdAt").asText("")).length() >= 7
                    ? node.path("period").asText(node.path("createdAt").asText("")).substring(0, 7)
                    : node.path("period").asText("1970-01");
            // Volume maps to `amount` for txn/topup/transfer/withdraw and to
            // `total_balance` for the saldo event; fall back to `total`.
            double volume = node.path("amount").asDouble(
                    node.path("total").asDouble(node.path("total_balance").asDouble(0d)));
            long cardId = node.path("cardId").asLong(node.path("card_id").asLong(0L));
            long merchantId = node.path("merchantId").asLong(node.path("merchant_id").asLong(0L));

            String eventType = node.path("event_type").asText("");
            String status = node.path("status").asText("");
            String entityId = deriveEntityId(node);
            String cardNumber = node.path("card_number").asText("");
            String paymentMethod = node.path("payment_method").asText("");
            String referenceNo = deriveReferenceNo(node);
            String sourceCard = node.path("source_card").asText("");
            String destinationCard = node.path("destination_card").asText("");
            double totalBalance = node.path("total_balance").asDouble(0d);
            // Lossless copy of the full payload so no analytics field is dropped.
            String details = record.value();

            writer.write(period, volume, cardId, merchantId, eventType, status, entityId,
                    cardNumber, paymentMethod, referenceNo, sourceCard, destinationCard,
                    totalBalance, details);
        } catch (Exception e) {
            log.error("failed to process stats event: {}", e.getMessage());
        } finally {
            ack.acknowledge();
        }
    }

    /**
     * Resolves the domain primary key from whichever id field the producer sent.
     */
    private String deriveEntityId(JsonNode node) {
        for (String key : new String[] {
                "transaction_id", "topup_id", "transfer_id", "withdraw_id",
                "merchant_id", "card_id", "cardId"}) {
            JsonNode v = node.path(key);
            if (!v.isMissingNode() && !v.asText("").isEmpty()) {
                return v.asText("");
            }
        }
        return "";
    }

    /**
     * Resolves the human-readable reference number from whichever no field the
     * producer sent (transaction_no / topup_no / transfer_no / withdraw_no).
     */
    private String deriveReferenceNo(JsonNode node) {
        for (String key : new String[] {
                "transaction_no", "topup_no", "transfer_no", "withdraw_no"}) {
            JsonNode v = node.path(key);
            if (!v.isMissingNode() && !v.asText("").isEmpty()) {
                return v.asText("");
            }
        }
        return "";
    }
}
