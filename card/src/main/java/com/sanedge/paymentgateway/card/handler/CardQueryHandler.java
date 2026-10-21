package com.sanedge.paymentgateway.card.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.card.ApiResponseCard;
import com.sanedge.paymentgateway.pb.card.ApiResponsePaginationCard;
import com.sanedge.paymentgateway.pb.card.ApiResponsePaginationCardDeleteAt;
import com.sanedge.paymentgateway.pb.card.CardQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardResponse;
import com.sanedge.paymentgateway.pb.card.CardResponseDeleteAt;
import com.sanedge.paymentgateway.pb.card.FindAllCardRequest;
import com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest;
import com.sanedge.paymentgateway.pb.card.FindByIdCardRequest;
import com.sanedge.paymentgateway.pb.card.FindByUserIdCardRequest;
import com.sanedge.paymentgateway.card.domain.requests.FindAllCards;
import com.sanedge.paymentgateway.card.service.CardQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardQueryHandler extends CardQueryServiceGrpc.CardQueryServiceImplBase {

    private final CardQueryService cardQueryService;

    @Override
    public void findAllCard(FindAllCardRequest request, StreamObserver<ApiResponsePaginationCard> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.card.domain.responses.CardResponse>> res = cardQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationCard.Builder builder = ApiResponsePaginationCard.newBuilder()
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
    public void findByIdCard(FindByIdCardRequest request, StreamObserver<ApiResponseCard> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.CardResponse> res = cardQueryService
                    .findById((long) request.getCardId());
            observer.onNext(toApiResponse(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByUserIdCard(FindByUserIdCardRequest request, StreamObserver<ApiResponseCard> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.CardResponse> res = cardQueryService
                    .findByUserId((long) request.getUserId());
            observer.onNext(toApiResponse(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActiveCard(FindAllCardRequest request,
            StreamObserver<ApiResponsePaginationCardDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.card.domain.responses.CardResponseDeleteAt>> res = cardQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationCardDeleteAt.Builder builder = ApiResponsePaginationCardDeleteAt.newBuilder()
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
    public void findByTrashedCard(FindAllCardRequest request,
            StreamObserver<ApiResponsePaginationCardDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.card.domain.responses.CardResponseDeleteAt>> res = cardQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationCardDeleteAt.Builder builder = ApiResponsePaginationCardDeleteAt.newBuilder()
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
    public void findByCardNumber(FindByCardNumberRequest request, StreamObserver<ApiResponseCard> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.CardResponse> res = cardQueryService
                    .findByCardNumber(request.getCardNumber());
            observer.onNext(toApiResponse(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private FindAllCards toFindAll(FindAllCardRequest request) {
        FindAllCards req = new FindAllCards();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private ApiResponseCard toApiResponse(
            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.CardResponse> res) {
        ApiResponseCard.Builder builder = ApiResponseCard.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            builder.setData(toProto(res.getData()));
        }
        return builder.build();
    }

    private CardResponse toProto(com.sanedge.paymentgateway.card.domain.responses.CardResponse r) {
        return CardResponse.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setUserId(r.getUserId() == null ? 0 : r.getUserId().intValue())
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setCardType(ProtoMappers.nvl(r.getCardType()))
                .setExpireDate(ProtoMappers.nvl(r.getExpireDate()))
                .setCvv(ProtoMappers.nvl(r.getCvv()))
                .setCardProvider(ProtoMappers.nvl(r.getCardProvider()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private CardResponseDeleteAt toProtoDeleteAt(
            com.sanedge.paymentgateway.card.domain.responses.CardResponseDeleteAt r) {
        return CardResponseDeleteAt.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setUserId(r.getUserId() == null ? 0 : r.getUserId().intValue())
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setCardType(ProtoMappers.nvl(r.getCardType()))
                .setExpireDate(ProtoMappers.nvl(r.getExpireDate()))
                .setCvv(ProtoMappers.nvl(r.getCvv()))
                .setCardProvider(ProtoMappers.nvl(r.getCardProvider()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(r.getDeletedAt())))
                .build();
    }
}
