package com.sanedge.paymentgateway.common.outbox;

/**
 * Lifecycle of a row in the shared {@code outbox} table (migration V30).
 *
 * <p>Port of the blueprint's per-module outbox status. Unlike the ecommerce
 * outboxes there is no lease: the publisher increments {@code attempts} and,
 * once {@code max-attempts} is reached, parks the row as {@link #FAILED}
 * (dead letter).
 */
public enum OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
