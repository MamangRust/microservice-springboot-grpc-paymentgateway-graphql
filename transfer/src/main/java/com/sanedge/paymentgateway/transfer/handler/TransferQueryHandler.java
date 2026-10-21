package com.sanedge.paymentgateway.transfer.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.transfer.ApiResponsePaginationTransfer;
import com.sanedge.paymentgateway.pb.transfer.ApiResponsePaginationTransferDeleteAt;
import com.sanedge.paymentgateway.pb.transfer.ApiResponseTransfer;
import com.sanedge.paymentgateway.pb.transfer.ApiResponseTransfers;
import com.sanedge.paymentgateway.pb.transfer.FindAllTransferRequest;
import com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest;
import com.sanedge.paymentgateway.pb.transfer.FindTransferByTransferFromRequest;
import com.sanedge.paymentgateway.pb.transfer.FindTransferByTransferToRequest;
import com.sanedge.paymentgateway.pb.transfer.TransferQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.transfer.TransferResponse;
import com.sanedge.paymentgateway.pb.transfer.TransferResponseDeleteAt;
import com.sanedge.paymentgateway.transfer.domain.requests.FindAllTransfers;
import com.sanedge.paymentgateway.transfer.service.TransferQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferQueryHandler extends TransferQueryServiceGrpc.TransferQueryServiceImplBase {

    private final TransferQueryService transferQueryService;

    @Override
    public void findAllTransfer(FindAllTransferRequest request, StreamObserver<ApiResponsePaginationTransfer> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse>> res = transferQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationTransfer.Builder builder = ApiResponsePaginationTransfer.newBuilder()
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
    public void findByIdTransfer(FindByIdTransferRequest request, StreamObserver<ApiResponseTransfer> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse> res = transferQueryService
                    .findById((long) request.getTransferId());

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
    public void findTransferByTransferFrom(FindTransferByTransferFromRequest request,
            StreamObserver<ApiResponseTransfers> observer) {
        try {
            ApiResponse<List<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse>> res = transferQueryService
                    .findByTransferFrom(request.getTransferFrom());

            ApiResponseTransfers.Builder builder = ApiResponseTransfers.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProto(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findTransferByTransferTo(FindTransferByTransferToRequest request,
            StreamObserver<ApiResponseTransfers> observer) {
        try {
            ApiResponse<List<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse>> res = transferQueryService
                    .findByTransferTo(request.getTransferTo());

            ApiResponseTransfers.Builder builder = ApiResponseTransfers.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProto(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActiveTransfer(FindAllTransferRequest request,
            StreamObserver<ApiResponsePaginationTransferDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponseDeleteAt>> res = transferQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationTransferDeleteAt.Builder builder = ApiResponsePaginationTransferDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashedTransfer(FindAllTransferRequest request,
            StreamObserver<ApiResponsePaginationTransferDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.transfer.domain.responses.TransferResponseDeleteAt>> res = transferQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationTransferDeleteAt.Builder builder = ApiResponsePaginationTransferDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private FindAllTransfers toFindAll(FindAllTransferRequest request) {
        FindAllTransfers req = new FindAllTransfers();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
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
}
