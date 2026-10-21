package com.sanedge.paymentgateway.saldo.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest;
import com.sanedge.paymentgateway.pb.saldo.ApiResponsePaginationSaldo;
import com.sanedge.paymentgateway.pb.saldo.ApiResponsePaginationSaldoDeleteAt;
import com.sanedge.paymentgateway.pb.saldo.ApiResponseSaldo;
import com.sanedge.paymentgateway.pb.saldo.FindAllSaldoRequest;
import com.sanedge.paymentgateway.pb.saldo.FindByIdSaldoRequest;
import com.sanedge.paymentgateway.pb.saldo.SaldoQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.saldo.SaldoResponse;
import com.sanedge.paymentgateway.pb.saldo.SaldoResponseDeleteAt;
import com.sanedge.paymentgateway.saldo.domain.requests.FindAllSaldos;
import com.sanedge.paymentgateway.saldo.service.SaldoQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaldoQueryHandler extends SaldoQueryServiceGrpc.SaldoQueryServiceImplBase {

    private final SaldoQueryService saldoQueryService;

    @Override
    public void findAllSaldo(FindAllSaldoRequest request, StreamObserver<ApiResponsePaginationSaldo> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse>> res = saldoQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationSaldo.Builder builder = ApiResponsePaginationSaldo.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByIdSaldo(FindByIdSaldoRequest request, StreamObserver<ApiResponseSaldo> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse> res = saldoQueryService
                    .findById((long) request.getSaldoId());

            ApiResponseSaldo.Builder builder = ApiResponseSaldo.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProto(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByCardNumber(FindByCardNumberRequest request, StreamObserver<ApiResponseSaldo> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse> res = saldoQueryService
                    .findByCard(request.getCardNumber());

            ApiResponseSaldo.Builder builder = ApiResponseSaldo.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProto(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActive(FindAllSaldoRequest request, StreamObserver<ApiResponsePaginationSaldoDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt>> res = saldoQueryService
                    .findActive(toFindAll(request));

            ApiResponsePaginationSaldoDeleteAt.Builder builder = ApiResponsePaginationSaldoDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashed(FindAllSaldoRequest request, StreamObserver<ApiResponsePaginationSaldoDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt>> res = saldoQueryService
                    .findTrashed(toFindAll(request));

            ApiResponsePaginationSaldoDeleteAt.Builder builder = ApiResponsePaginationSaldoDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private FindAllSaldos toFindAll(FindAllSaldoRequest request) {
        FindAllSaldos req = new FindAllSaldos();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private SaldoResponse toProto(com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse r) {
        return SaldoResponse.newBuilder()
                .setSaldoId(r.getId() == null ? 0 : r.getId().intValue())
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setTotalBalance(r.getTotalBalance() == null ? 0 : r.getTotalBalance().intValue())
                .setWithdrawTime(ProtoMappers.nvl(r.getWithdrawTime()))
                .setWithdrawAmount(r.getWithdrawAmount() == null ? 0 : r.getWithdrawAmount().intValue())
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private SaldoResponseDeleteAt toProtoDeleteAt(
            com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt r) {
        return SaldoResponseDeleteAt.newBuilder()
                .setSaldoId(r.getId() == null ? 0 : r.getId().intValue())
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setTotalBalance(r.getTotalBalance() == null ? 0 : r.getTotalBalance().intValue())
                .setWithdrawTime(ProtoMappers.nvl(r.getWithdrawTime()))
                .setWithdrawAmount(r.getWithdrawAmount() == null ? 0 : r.getWithdrawAmount().intValue())
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(r.getDeletedAt())))
                .build();
    }
}
