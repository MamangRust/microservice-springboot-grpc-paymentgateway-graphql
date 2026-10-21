package com.sanedge.paymentgateway.saldo.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.saldo.ApiResponseSaldo;
import com.sanedge.paymentgateway.pb.saldo.ApiResponseSaldoAll;
import com.sanedge.paymentgateway.pb.saldo.ApiResponseSaldoDelete;
import com.sanedge.paymentgateway.pb.saldo.ApiResponseSaldoDeleteAt;
import com.sanedge.paymentgateway.pb.saldo.CreateSaldoRequest;
import com.sanedge.paymentgateway.pb.saldo.FindByIdSaldoRequest;
import com.sanedge.paymentgateway.pb.saldo.SaldoCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.saldo.SaldoResponse;
import com.sanedge.paymentgateway.pb.saldo.SaldoResponseDeleteAt;
import com.sanedge.paymentgateway.pb.saldo.UpdateSaldoBalanceRequest;
import com.sanedge.paymentgateway.pb.saldo.UpdateSaldoRequest;
import com.sanedge.paymentgateway.pb.saldo.UpdateSaldoWithdrawRequest;
import com.sanedge.paymentgateway.saldo.service.SaldoCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaldoCommandHandler extends SaldoCommandServiceGrpc.SaldoCommandServiceImplBase {

    private final SaldoCommandService saldoCommandService;

    @Override
    public void createSaldo(CreateSaldoRequest request, StreamObserver<ApiResponseSaldo> observer) {
        try {
            com.sanedge.paymentgateway.saldo.domain.requests.CreateSaldoRequest req = new com.sanedge.paymentgateway.saldo.domain.requests.CreateSaldoRequest();
            req.setCardNumber(request.getCardNumber());
            req.setTotalBalance((long) request.getTotalBalance());

            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse> res = saldoCommandService
                    .create(req);
            observer.onNext(toApiResponseSaldo(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void updateSaldo(UpdateSaldoRequest request, StreamObserver<ApiResponseSaldo> observer) {
        try {
            com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoRequest req = new com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoRequest();
            req.setSaldoId((long) request.getSaldoId());
            req.setCardNumber(request.getCardNumber());
            req.setTotalBalance((long) request.getTotalBalance());

            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse> res = saldoCommandService
                    .update(req);
            observer.onNext(toApiResponseSaldo(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void updateSaldoBalance(UpdateSaldoBalanceRequest request, StreamObserver<ApiResponseSaldo> observer) {
        try {
            com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoBalance req = new com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoBalance();
            req.setCardNumber(request.getCardNumber());
            req.setTotalBalance((long) request.getTotalBalance());

            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse> res = saldoCommandService
                    .updateBalance(req);
            observer.onNext(toApiResponseSaldo(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void updateSaldoWithdraw(UpdateSaldoWithdrawRequest request, StreamObserver<ApiResponseSaldo> observer) {
        try {
            com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoWithdraw req = new com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoWithdraw();
            req.setCardNumber(request.getCardNumber());
            req.setTotalBalance((long) request.getTotalBalance());
            req.setWithdrawAmount((long) request.getWithdrawAmount());
            req.setWithdrawTime(parseDateTime(request.getWithdrawTime()));

            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse> res = saldoCommandService
                    .updateWithdraw(req);
            observer.onNext(toApiResponseSaldo(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void trashedSaldo(FindByIdSaldoRequest request, StreamObserver<ApiResponseSaldoDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt> res = saldoCommandService
                    .trash((long) request.getSaldoId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreSaldo(FindByIdSaldoRequest request, StreamObserver<ApiResponseSaldoDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt> res = saldoCommandService
                    .restore((long) request.getSaldoId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteSaldoPermanent(FindByIdSaldoRequest request, StreamObserver<ApiResponseSaldoDelete> observer) {
        try {
            ApiResponse<Boolean> res = saldoCommandService.delete((long) request.getSaldoId());
            observer.onNext(ApiResponseSaldoDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllSaldo(Empty request, StreamObserver<ApiResponseSaldoAll> observer) {
        try {
            ApiResponse<Boolean> res = saldoCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllSaldoPermanent(Empty request, StreamObserver<ApiResponseSaldoAll> observer) {
        try {
            ApiResponse<Boolean> res = saldoCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponseSaldo toApiResponseSaldo(
            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse> res) {
        ApiResponseSaldo.Builder builder = ApiResponseSaldo.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            builder.setData(toProto(res.getData()));
        }
        return builder.build();
    }

    private ApiResponseSaldoDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt> res) {
        SaldoResponseDeleteAt.Builder data = SaldoResponseDeleteAt.newBuilder();
        if (res.getData() != null) {
            data.setSaldoId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                    .setCardNumber(ProtoMappers.nvl(res.getData().getCardNumber()))
                    .setTotalBalance(res.getData().getTotalBalance() == null ? 0
                            : res.getData().getTotalBalance().intValue())
                    .setWithdrawTime(ProtoMappers.nvl(res.getData().getWithdrawTime()))
                    .setWithdrawAmount(res.getData().getWithdrawAmount() == null ? 0
                            : res.getData().getWithdrawAmount().intValue())
                    .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                    .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                    .setDeletedAt(StringValue.of(ProtoMappers.nvl(res.getData().getDeletedAt())));
        }
        return ApiResponseSaldoDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(data)
                .build();
    }

    private ApiResponseSaldoAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseSaldoAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
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

    private java.time.LocalDateTime parseDateTime(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDateTime.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
