package com.sanedge.paymentgateway.gateway.graphql;

import java.time.LocalDate;
import java.util.List;

import com.sanedge.paymentgateway.common.domain.responses.api.PaginationMeta;
import com.sanedge.paymentgateway.gateway.graphql.dto.Page;

/**
 * Small helpers reused by the GraphQL resolvers, mirroring the {@code nvl}
 * helpers that used to live in each REST controller.
 */
public final class Gql {

    private Gql() {
    }

    public static int i(Integer value) {
        return value == null ? 0 : value;
    }

    public static int i(Long value) {
        return value == null ? 0 : value.intValue();
    }

    public static int i(Double value) {
        return value == null ? 0 : value.intValue();
    }

    public static long l(Long value) {
        return value == null ? 0L : value;
    }

    public static String s(String value) {
        return value == null ? "" : value;
    }

    /** Parses an ISO {@code yyyy-MM-dd} date, returning {@code null} when blank. */
    public static LocalDate date(String value) {
        return (value == null || value.isBlank()) ? null : LocalDate.parse(value);
    }

    public static Boolean ok(String status) {
        return "success".equalsIgnoreCase(status);
    }

    public static <T> Page<T> page(List<T> data, PaginationMeta pagination) {
        return new Page<>(data, pagination);
    }
}
