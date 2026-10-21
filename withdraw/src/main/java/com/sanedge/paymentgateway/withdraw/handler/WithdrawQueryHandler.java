package com.sanedge.paymentgateway.withdraw.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest;
import com.sanedge.paymentgateway.pb.withdraw.ApiResponsePaginationWithdraw;
import com.sanedge.paymentgateway.pb.withdraw.ApiResponsePaginationWithdrawDeleteAt;
import com.sanedge.paymentgateway.pb.withdraw.ApiResponseWithdraw;
import com.sanedge.paymentgateway.pb.withdraw.ApiResponsesWithdraw;
import com.sanedge.paymentgateway.pb.withdraw.FindAllWithdrawByCardNumberRequest;
import com.sanedge.paymentgateway.pb.withdraw.FindAllWithdrawRequest;
import com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawResponse;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawResponseDeleteAt;
import com.sanedge.paymentgateway.withdraw.domain.requests.FindAllWithdrawCardNumber;
import com.sanedge.paymentgateway.withdraw.domain.requests.FindAllWithdraws;
import com.sanedge.paymentgateway.withdraw.service.WithdrawQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WithdrawQueryHandler extends WithdrawQueryServiceGrpc.WithdrawQueryServiceImplBase {

    private final WithdrawQueryService withdrawQueryService;

    @Override
    public void findAllWithdraw(FindAllWithdrawRequest request,
            StreamObserver<ApiResponsePaginationWithdraw> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse>> res = withdrawQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationWithdraw.Builder builder = ApiResponsePaginationWithdraw.newBuilder()
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
    public void findAllWithdrawByCardNumber(FindAllWithdrawByCardNumberRequest request,
            StreamObserver<ApiResponsePaginationWithdraw> observer) {
        try {
            FindAllWithdrawCardNumber req = new FindAllWithdrawCardNumber();
            req.setCardNumber(request.getCardNumber());
            req.setPage(request.getPage());
            req.setPageSize(request.getPageSize());
            req.setSearch(request.getSearch());

            ApiResponsePagination<List<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse>> res = withdrawQueryService
                    .findAllByCardNumber(req);

            ApiResponsePaginationWithdraw.Builder builder = ApiResponsePaginationWithdraw.newBuilder()
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
    public void findByIdWithdraw(FindByIdWithdrawRequest request, StreamObserver<ApiResponseWithdraw> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse> res = withdrawQueryService
                    .findById((long) request.getWithdrawId());

            observer.onNext(ApiResponseWithdraw.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setData(toProto(res.getData()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByCardNumber(FindByCardNumberRequest request, StreamObserver<ApiResponsesWithdraw> observer) {
        try {
            ApiResponse<List<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse>> res = withdrawQueryService
                    .findByCard(request.getCardNumber());

            ApiResponsesWithdraw.Builder builder = ApiResponsesWithdraw.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActive(FindAllWithdrawRequest request,
            StreamObserver<ApiResponsePaginationWithdrawDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponseDeleteAt>> res = withdrawQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationWithdrawDeleteAt.Builder builder = ApiResponsePaginationWithdrawDeleteAt.newBuilder()
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
    public void findByTrashed(FindAllWithdrawRequest request,
            StreamObserver<ApiResponsePaginationWithdrawDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponseDeleteAt>> res = withdrawQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationWithdrawDeleteAt.Builder builder = ApiResponsePaginationWithdrawDeleteAt.newBuilder()
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

    private FindAllWithdraws toFindAll(FindAllWithdrawRequest request) {
        FindAllWithdraws req = new FindAllWithdraws();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private WithdrawResponse toProto(com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse r) {
        if (r == null) {
            return WithdrawResponse.getDefaultInstance();
        }
        return WithdrawResponse.newBuilder()
                .setWithdrawId(r.getId() == null ? 0 : r.getId().intValue())
                .setWithdrawNo(ProtoMappers.nvl(r.getWithdrawNo()))
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setWithdrawAmount(r.getWithdrawAmount() == null ? 0 : r.getWithdrawAmount().intValue())
                .setWithdrawTime(ProtoMappers.nvl(r.getWithdrawTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private WithdrawResponseDeleteAt toProtoDeleteAt(
            com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponseDeleteAt r) {
        if (r == null) {
            return WithdrawResponseDeleteAt.getDefaultInstance();
        }
        return WithdrawResponseDeleteAt.newBuilder()
                .setWithdrawId(r.getId() == null ? 0 : r.getId().intValue())
                .setWithdrawNo(ProtoMappers.nvl(r.getWithdrawNo()))
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setWithdrawAmount(r.getWithdrawAmount() == null ? 0 : r.getWithdrawAmount().intValue())
                .setWithdrawTime(ProtoMappers.nvl(r.getWithdrawTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(r.getDeletedAt())))
                .build();
    }
}
