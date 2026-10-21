package com.sanedge.paymentgateway.transfer.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.transfer.ApiResponseTransfer;
import com.sanedge.paymentgateway.pb.transfer.ApiResponseTransferAll;
import com.sanedge.paymentgateway.pb.transfer.ApiResponseTransferDelete;
import com.sanedge.paymentgateway.pb.transfer.ApIResponseTransferDeleteAt;
import com.sanedge.paymentgateway.pb.transfer.CreateTransferRequest;
import com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest;
import com.sanedge.paymentgateway.pb.transfer.TransferCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.transfer.TransferResponse;
import com.sanedge.paymentgateway.pb.transfer.TransferResponseDeleteAt;
import com.sanedge.paymentgateway.pb.transfer.UpdateTransferRequest;
import com.sanedge.paymentgateway.transfer.service.TransferCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferCommandHandler extends TransferCommandServiceGrpc.TransferCommandServiceImplBase {

    private final TransferCommandService transferCommandService;

    @Override
    public void createTransfer(CreateTransferRequest request, StreamObserver<ApiResponseTransfer> observer) {
        try {
            com.sanedge.paymentgateway.transfer.domain.requests.CreateTransferRequest req = new com.sanedge.paymentgateway.transfer.domain.requests.CreateTransferRequest();
            req.setTransferFrom(request.getTransferFrom());
            req.setTransferTo(request.getTransferTo());
            req.setTransferAmount((long) request.getTransferAmount());

            ApiResponse<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse> res = transferCommandService
                    .create(req);

            ApiResponseTransfer.Builder builder = ApiResponseTransfer.newBuilder()
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
    public void updateTransfer(UpdateTransferRequest request, StreamObserver<ApiResponseTransfer> observer) {
        try {
            com.sanedge.paymentgateway.transfer.domain.requests.UpdateTransferRequest req = new com.sanedge.paymentgateway.transfer.domain.requests.UpdateTransferRequest();
            req.setTransferId((long) request.getTransferId());
            req.setTransferFrom(request.getTransferFrom());
            req.setTransferTo(request.getTransferTo());
            req.setTransferAmount((long) request.getTransferAmount());

            ApiResponse<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse> res = transferCommandService
                    .update(req);

            ApiResponseTransfer.Builder builder = ApiResponseTransfer.newBuilder()
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
    public void trashedTransfer(FindByIdTransferRequest request, StreamObserver<ApIResponseTransferDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponseDeleteAt> res = transferCommandService
                    .trashed((long) request.getTransferId());

            ApIResponseTransferDeleteAt.Builder builder = ApIResponseTransferDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProtoDeleteAt(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreTransfer(FindByIdTransferRequest request, StreamObserver<ApIResponseTransferDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponseDeleteAt> res = transferCommandService
                    .restore((long) request.getTransferId());

            ApIResponseTransferDeleteAt.Builder builder = ApIResponseTransferDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProtoDeleteAt(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteTransferPermanent(FindByIdTransferRequest request,
            StreamObserver<ApiResponseTransferDelete> observer) {
        try {
            ApiResponse<Boolean> res = transferCommandService.deletePermanent((long) request.getTransferId());
            observer.onNext(ApiResponseTransferDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllTransfer(Empty request, StreamObserver<ApiResponseTransferAll> observer) {
        try {
            ApiResponse<Boolean> res = transferCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllTransferPermanent(Empty request, StreamObserver<ApiResponseTransferAll> observer) {
        try {
            ApiResponse<Boolean> res = transferCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private TransferResponse toProto(com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse r) {
        return TransferResponse.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setTransferNo(ProtoMappers.nvl(r.getTransferNo()))
                .setTransferFrom(ProtoMappers.nvl(r.getTransferFrom()))
                .setTransferTo(ProtoMappers.nvl(r.getTransferTo()))
                .setTransferAmount(r.getTransferAmount() == null ? 0 : r.getTransferAmount().intValue())
                .setTransferTime(ProtoMappers.nvl(r.getTransferTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private TransferResponseDeleteAt toProtoDeleteAt(
            com.sanedge.paymentgateway.transfer.domain.responses.TransferResponseDeleteAt r) {
        return TransferResponseDeleteAt.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setTransferNo(ProtoMappers.nvl(r.getTransferNo()))
                .setTransferFrom(ProtoMappers.nvl(r.getTransferFrom()))
                .setTransferTo(ProtoMappers.nvl(r.getTransferTo()))
                .setTransferAmount(r.getTransferAmount() == null ? 0 : r.getTransferAmount().intValue())
                .setTransferTime(ProtoMappers.nvl(r.getTransferTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(r.getDeletedAt())))
                .build();
    }

    private ApiResponseTransferAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseTransferAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
    }
}
