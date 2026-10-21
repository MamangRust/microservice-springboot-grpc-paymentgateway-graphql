package com.sanedge.paymentgateway.topup.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.topup.ApiResponsePaginationTopup;
import com.sanedge.paymentgateway.pb.topup.ApiResponsePaginationTopupDeleteAt;
import com.sanedge.paymentgateway.pb.topup.ApiResponseTopup;
import com.sanedge.paymentgateway.pb.topup.FindAllTopupByCardNumberRequest;
import com.sanedge.paymentgateway.pb.topup.FindAllTopupRequest;
import com.sanedge.paymentgateway.pb.topup.FindByCardNumberTopupRequest;
import com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest;
import com.sanedge.paymentgateway.pb.topup.TopupQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.topup.TopupResponse;
import com.sanedge.paymentgateway.pb.topup.TopupResponseDeleteAt;
import com.sanedge.paymentgateway.topup.domain.requests.FindAllTopups;
import com.sanedge.paymentgateway.topup.domain.requests.FindAllTopupsByCardNumber;
import com.sanedge.paymentgateway.topup.service.TopupQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TopupQueryHandler extends TopupQueryServiceGrpc.TopupQueryServiceImplBase {

    private final TopupQueryService topupQueryService;

    @Override
    public void findAllTopup(FindAllTopupRequest request, StreamObserver<ApiResponsePaginationTopup> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.topup.domain.responses.TopupResponse>> res = topupQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationTopup.Builder builder = ApiResponsePaginationTopup.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(TopupCommandHandler.toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findAllTopupByCardNumber(FindAllTopupByCardNumberRequest request,
            StreamObserver<ApiResponsePaginationTopup> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.topup.domain.responses.TopupResponse>> res = topupQueryService
                    .findAllByCardNumber(toFindAllByCard(request));

            ApiResponsePaginationTopup.Builder builder = ApiResponsePaginationTopup.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(TopupCommandHandler.toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByIdTopup(FindByIdTopupRequest request, StreamObserver<ApiResponseTopup> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.topup.domain.responses.TopupResponse> res = topupQueryService
                    .findById((long) request.getTopupId());

            ApiResponseTopup.Builder builder = ApiResponseTopup.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(TopupCommandHandler.toProto(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByCardNumberTopup(FindByCardNumberTopupRequest request, StreamObserver<ApiResponseTopup> observer) {
        try {
            ApiResponse<List<com.sanedge.paymentgateway.topup.domain.responses.TopupResponse>> res = topupQueryService
                    .findByCard(request.getCardNumber());

            ApiResponseTopup.Builder builder = ApiResponseTopup.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null && !res.getData().isEmpty()) {
                builder.setData(TopupCommandHandler.toProto(res.getData().get(0)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActive(FindAllTopupRequest request, StreamObserver<ApiResponsePaginationTopupDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.topup.domain.responses.TopupResponseDeleteAt>> res = topupQueryService
                    .findActive(toFindAll(request));

            ApiResponsePaginationTopupDeleteAt.Builder builder = ApiResponsePaginationTopupDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(TopupCommandHandler.toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashed(FindAllTopupRequest request, StreamObserver<ApiResponsePaginationTopupDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.topup.domain.responses.TopupResponseDeleteAt>> res = topupQueryService
                    .findTrashed(toFindAll(request));

            ApiResponsePaginationTopupDeleteAt.Builder builder = ApiResponsePaginationTopupDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(TopupCommandHandler.toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private FindAllTopups toFindAll(FindAllTopupRequest request) {
        FindAllTopups req = new FindAllTopups();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private FindAllTopupsByCardNumber toFindAllByCard(FindAllTopupByCardNumberRequest request) {
        FindAllTopupsByCardNumber req = new FindAllTopupsByCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }
}
