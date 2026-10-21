package com.sanedge.paymentgateway.statswriter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * One-shot backfill: reads aggregated volume from the source PostgreSQL
 * (financial DB) and writes it into the ClickHouse {@code stats_volume} table.
 * Enabled with {@code app.backfill.enabled=true} (the k8s Job runs the
 * stats-writer image with that flag and {@code spring.main.web-application-type=none}).
 *
 * <p>Idempotent: rows are re-inserted (ClickHouse is append-only), so run once
 * per backfill window.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.backfill.enabled", havingValue = "true")
public class StatsBackfillRunner implements CommandLineRunner {

    private final JdbcTemplate sourceJdbcTemplate;
    private final StatsClickHouseWriter writer;

    @Value("${backfill.sql:SELECT to_char(created_at, 'YYYY-MM') AS period, "
            + "COALESCE(card_id, 0) AS card_id, COALESCE(merchant_id, 0) AS merchant_id, "
            + "SUM(amount) AS volume FROM transactions GROUP BY 1, 2, 3}")
    private String backfillSql;

    @Override
    public void run(String... args) {
        log.info("stats-backfill starting");
        writer.ensureTable();
        int count = sourceJdbcTemplate.query(backfillSql, (rs, i) -> new Object[]{
                rs.getString("period"),
                rs.getLong("card_id"),
                rs.getLong("merchant_id"),
                rs.getDouble("volume")
        }).stream().mapToInt(row -> {
            writer.write((String) row[0], (Double) row[3], (Long) row[1], (Long) row[2]);
            return 1;
        }).sum();
        log.info("stats-backfill completed: {} rows written to ClickHouse", count);
    }
}
