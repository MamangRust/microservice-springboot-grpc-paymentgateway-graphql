package com.sanedge.paymentgateway.common.outbox;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Builds the JSON envelope every email event must carry.
 *
 * <p>Identical contract to the ecommerce/pointofsale factory: the consumer
 * ({@code EmailService.hasValidEnvelope}) requires {@code event_id},
 * {@code schema_version == 1}, {@code event_type}, {@code email},
 * {@code subject}, {@code body}, plus {@code occurred_at} for observability.
 */
public final class OutboxEventFactory {

    /** Must match {@code EmailService.EVENT_SCHEMA_VERSION}. */
    public static final int SCHEMA_VERSION = 1;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OutboxEventFactory() {
    }

    /**
     * Builds the email envelope for {@code topic}.
     *
     * @return the serialized payload plus the generated {@code event_id}
     */
    public static Envelope email(String topic, String email, String subject, String body) {
        String eventId = UUID.randomUUID().toString();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("subject", subject);
        payload.put("body", body);
        payload.put("event_id", eventId);
        payload.put("schema_version", SCHEMA_VERSION);
        payload.put("event_type", topic);
        payload.put("occurred_at", Instant.now().toString());

        try {
            return new Envelope(eventId, MAPPER.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize outbox payload for topic " + topic, e);
        }
    }

    /** The generated event id plus its serialized JSON payload. */
    public record Envelope(String eventId, String payload) {
    }

    /**
     * Builds the analytics envelope published to {@code stats.<domain>.event}.
     *
     * <p>Consumed by {@code stats-writer} ({@code StatsWriterConsumer}) and written
     * into the ClickHouse {@code stats_volume} table. Fields mirror what the
     * consumer reads: {@code period} (YYYY-MM), {@code amount}, {@code cardId},
     * {@code merchantId}.
     *
     * @param topic      destination topic; also used as {@code event_type}
     * @param period     accounting period, formatted {@code YYYY-MM}
     * @param amount     revenue / volume for the period
     * @param cardId     card dimension id (0 if not applicable)
     * @param merchantId merchant dimension id (0 if not applicable)
     * @return the serialized payload plus the generated {@code event_id}
     */
    public static Envelope stats(String topic, String period, double amount, long cardId, long merchantId) {
        String eventId = UUID.randomUUID().toString();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("period", period);
        payload.put("amount", amount);
        payload.put("cardId", cardId);
        payload.put("merchantId", merchantId);
        payload.put("event_id", eventId);
        payload.put("schema_version", SCHEMA_VERSION);
        payload.put("event_type", topic);
        payload.put("occurred_at", Instant.now().toString());

        try {
            return new Envelope(eventId, MAPPER.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize stats payload for topic " + topic, e);
        }
    }

    /**
     * Builds the rich {@code stats.payment.<domain>.event} envelope used by the
     * payment_gateway analytics producers.
     *
     * <p>Unlike {@link #stats} (which carries only period/amount/dimensions), this
     * variant carries the domain-specific fields the payment audits require (e.g.
     * {@code transaction_id}, {@code card_number}, {@code status}) while still
     * including the compatibility fields {@code period}/{@code amount}/{@code cardId}/
     * {@code merchantId} so the existing {@code stats-writer} parser keeps working.
     *
     * <p>Common envelope keys ({@code event_id}, {@code schema_version},
     * {@code event_type}, {@code occurred_at}) are always appended; caller keys
     * that collide with them are overwritten to keep the contract single-sourced.
     *
     * @param topic destination topic; also used as {@code event_type}
     * @param fields domain-specific key/value pairs, inserted in iteration order
     * @return the serialized payload plus the generated {@code event_id}
     */
    public static Envelope statsRich(String topic, Map<String, Object> fields) {
        String eventId = UUID.randomUUID().toString();

        Map<String, Object> payload = new LinkedHashMap<>();
        if (fields != null) {
            payload.putAll(fields);
        }
        payload.put("event_id", eventId);
        payload.put("schema_version", SCHEMA_VERSION);
        payload.put("event_type", topic);
        payload.put("occurred_at", Instant.now().toString());

        try {
            return new Envelope(eventId, MAPPER.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize stats payload for topic " + topic, e);
        }
    }
}
