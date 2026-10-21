package com.sanedge.paymentgateway.statsreader.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.stats.StatItem;
import com.sanedge.paymentgateway.pb.stats.StatsReaderServiceGrpc;
import com.sanedge.paymentgateway.pb.stats.StatsRequest;
import com.sanedge.paymentgateway.pb.stats.StatsResponse;
import com.sanedge.paymentgateway.statsreader.repository.StatsClickHouseRepository;
import com.sanedge.paymentgateway.statsreader.repository.StatsClickHouseRepository.StatRow;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatsReaderHandler extends StatsReaderServiceGrpc.StatsReaderServiceImplBase {

    private final StatsClickHouseRepository repository;

    @Override
    public void findMonthlyTotalRevenue(StatsRequest request, StreamObserver<StatsResponse> observer) {
        try {
            List<StatRow> rows = repository.monthlyTotalRevenue();
            observer.onNext(build(rows));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyVolume(StatsRequest request, StreamObserver<StatsResponse> observer) {
        try {
            observer.onNext(build(repository.yearlyVolume()));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findCardStats(StatsRequest request, StreamObserver<StatsResponse> observer) {
        try {
            observer.onNext(build(repository.cardStats()));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMerchantVolume(StatsRequest request, StreamObserver<StatsResponse> observer) {
        try {
            observer.onNext(build(repository.merchantVolume()));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private StatsResponse build(List<StatRow> rows) {
        StatsResponse.Builder builder = StatsResponse.newBuilder()
                .setStatus(ProtoMappers.nvl("success"))
                .setMessage(ProtoMappers.nvl("OK"));
        for (StatRow r : rows) {
            builder.addData(StatItem.newBuilder()
                    .setPeriod(ProtoMappers.nvl(r.period()))
                    .setDimensionId(r.dimensionId())
                    .setValue(r.value())
                    .build());
        }
        return builder.build();
    }
}
