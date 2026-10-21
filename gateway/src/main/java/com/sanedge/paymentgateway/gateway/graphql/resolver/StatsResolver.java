package com.sanedge.paymentgateway.gateway.graphql.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.sanedge.paymentgateway.gateway.dto.responses.stats.StatItemResponse;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.stats.StatsReaderServiceGrpc;
import com.sanedge.paymentgateway.pb.stats.StatsRequest;

import lombok.RequiredArgsConstructor;

/**
 * GraphQL facade over the ClickHouse-backed {@code stats-reader} gRPC service.
 */
@Controller
@RequiredArgsConstructor
public class StatsResolver {

    private final StatsReaderServiceGrpc.StatsReaderServiceBlockingStub statsReaderStub;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public List<StatItemResponse> monthlyTotalRevenue(@Argument Integer year, @Argument Integer month) {
        var resp = statsReaderStub.findMonthlyTotalRevenue(StatsRequest.newBuilder()
                .setYear(Gql.i(year)).setMonth(Gql.i(month)).build());
        return ProtoMapper.statItems(resp.getDataList());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public List<StatItemResponse> cardStats(@Argument Long cardId, @Argument Integer year) {
        var resp = statsReaderStub.findCardStats(StatsRequest.newBuilder()
                .setCardId(Gql.l(cardId)).setYear(Gql.i(year)).build());
        return ProtoMapper.statItems(resp.getDataList());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public List<StatItemResponse> yearlyVolume(@Argument Integer year) {
        var resp = statsReaderStub.findYearlyVolume(StatsRequest.newBuilder()
                .setYear(Gql.i(year)).build());
        return ProtoMapper.statItems(resp.getDataList());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public List<StatItemResponse> merchantVolume(@Argument Long merchantId, @Argument Integer year) {
        var resp = statsReaderStub.findMerchantVolume(StatsRequest.newBuilder()
                .setMerchantId(Gql.l(merchantId)).setYear(Gql.i(year)).build());
        return ProtoMapper.statItems(resp.getDataList());
    }
}
