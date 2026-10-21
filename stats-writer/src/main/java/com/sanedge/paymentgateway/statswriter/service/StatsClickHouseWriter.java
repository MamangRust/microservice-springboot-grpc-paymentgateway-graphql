package com.sanedge.paymentgateway.statswriter.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Writes analytics events into the ClickHouse {@code stats_volume} table.
 *
 * <p>The base columns ({@code period}, {@code volume}, {@code card_id},
 * {@code merchant_id}, {@code metric}) keep the legacy {@code stats-reader}
 * aggregation queries working. The richer {@code stats.payment.*.event}
 * producers (see {@code OutboxEventFactory.statsRich}) also carry domain
 * fields ({@code card_number}, {@code status}, {@code transaction_id}, ...);
 * those are absorbed into dedicated columns plus a lossless {@code details}
 * JSON blob so no analytics signal is dropped.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class StatsClickHouseWriter {

    private final JdbcTemplate clickHouseJdbcTemplate;

    public void ensureTable() {
        clickHouseJdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS stats_volume ("
                        + "period String, "
                        + "volume Float64, "
                        + "card_id Int64 DEFAULT 0, "
                        + "merchant_id Int64 DEFAULT 0, "
                        + "metric String DEFAULT 'txn', "
                        + "event_type String DEFAULT '', "
                        + "status String DEFAULT '', "
                        + "entity_id String DEFAULT '', "
                        + "card_number String DEFAULT '', "
                        + "payment_method String DEFAULT '', "
                        + "reference_no String DEFAULT '', "
                        + "source_card String DEFAULT '', "
                        + "destination_card String DEFAULT '', "
                        + "total_balance Float64 DEFAULT 0, "
                        + "details String DEFAULT ''"
                        + ") ENGINE = MergeTree() ORDER BY (metric, period)");

        // Existing tables created before this migration lack the new columns.
        // ClickHouse supports idempotent ADD COLUMN IF NOT EXISTS; apply each
        // independently so one failure does not abort the rest.
        addColumnIfNotExists("event_type", "String DEFAULT ''");
        addColumnIfNotExists("status", "String DEFAULT ''");
        addColumnIfNotExists("entity_id", "String DEFAULT ''");
        addColumnIfNotExists("card_number", "String DEFAULT ''");
        addColumnIfNotExists("payment_method", "String DEFAULT ''");
        addColumnIfNotExists("reference_no", "String DEFAULT ''");
        addColumnIfNotExists("source_card", "String DEFAULT ''");
        addColumnIfNotExists("destination_card", "String DEFAULT ''");
        addColumnIfNotExists("total_balance", "Float64 DEFAULT 0");
        addColumnIfNotExists("details", "String DEFAULT ''");
    }

    private void addColumnIfNotExists(String column, String definition) {
        try {
            clickHouseJdbcTemplate.execute(
                    "ALTER TABLE stats_volume ADD COLUMN IF NOT EXISTS " + column + " " + definition);
        } catch (Exception e) {
            log.warn("could not add column {} to stats_volume (may already exist): {}", column, e.getMessage());
        }
    }

    /**
     * Legacy write used by the backfill runner: only the base columns are
     * populated; the rich columns default to empty/zero.
     */
    public void write(String period, double volume, long cardId, long merchantId) {
        write(period, volume, cardId, merchantId, "", "", "", "", "", "", "", "", 0d, "");
    }

    /**
     * Rich write used by {@link StatsWriterConsumer} for the
     * {@code stats.payment.*.event} payloads. Absorbs every domain field the
     * producers emit.
     */
    public void write(String period, double volume, long cardId, long merchantId,
            String eventType, String status, String entityId,
            String cardNumber, String paymentMethod, String referenceNo,
            String sourceCard, String destinationCard, double totalBalance, String details) {
        clickHouseJdbcTemplate.update(
                "INSERT INTO stats_volume (period, volume, card_id, merchant_id, metric, "
                        + "event_type, status, entity_id, card_number, payment_method, reference_no, "
                        + "source_card, destination_card, total_balance, details) "
                        + "VALUES (?, ?, ?, ?, 'txn', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                period, volume, cardId, merchantId,
                eventType, status, entityId, cardNumber, paymentMethod, referenceNo,
                sourceCard, destinationCard, totalBalance, details);
    }
}
