package com.sanedge.paymentgateway.card.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.card.ApiResponseCard;
import com.sanedge.paymentgateway.pb.card.ApiResponseCardAll;
import com.sanedge.paymentgateway.pb.card.ApiResponseCardDelete;
import com.sanedge.paymentgateway.pb.card.ApiResponseCardDeleteAt;
import com.sanedge.paymentgateway.pb.card.CardCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardResponse;
import com.sanedge.paymentgateway.pb.card.CardResponseDeleteAt;
import com.sanedge.paymentgateway.pb.card.CreateCardRequest;
import com.sanedge.paymentgateway.pb.card.FindByIdCardRequest;
import com.sanedge.paymentgateway.pb.card.UpdateCardRequest;
import com.sanedge.paymentgateway.card.service.CardCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardCommandHandler extends CardCommandServiceGrpc.CardCommandServiceImplBase {

    private final CardCommandService cardCommandService;

    @Override
    public void createCard(CreateCardRequest request, StreamObserver<ApiResponseCard> observer) {
        try {
            com.sanedge.paymentgateway.card.domain.requests.CreateCardRequest req = new com.sanedge.paymentgateway.card.domain.requests.CreateCardRequest();
            req.setUserId((long) request.getUserId());
            req.setCardType(request.getCardType());
            req.setExpireDate(toLocalDate(request.getExpireDate()));
            req.setCvv(request.getCvv());
            req.setCardProvider(request.getCardProvider());

            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.CardResponse> res = cardCommandService
                    .createCard(req);

            ApiResponseCard.Builder builder = ApiResponseCard.newBuilder()
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
    public void updateCard(UpdateCardRequest request, StreamObserver<ApiResponseCard> observer) {
        try {
            com.sanedge.paymentgateway.card.domain.requests.UpdateCardRequest req = new com.sanedge.paymentgateway.card.domain.requests.UpdateCardRequest();
            req.setCardId((long) request.getCardId());
            req.setUserId((long) request.getUserId());
            req.setCardType(request.getCardType());
            req.setExpireDate(toLocalDate(request.getExpireDate()));
            req.setCvv(request.getCvv());
            req.setCardProvider(request.getCardProvider());

            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.CardResponse> res = cardCommandService
                    .updateCard(req);

            ApiResponseCard.Builder builder = ApiResponseCard.newBuilder()
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
    public void trashedCard(FindByIdCardRequest request, StreamObserver<ApiResponseCardDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.CardResponseDeleteAt> res = cardCommandService
                    .trashCard((long) request.getCardId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreCard(FindByIdCardRequest request, StreamObserver<ApiResponseCardDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.CardResponseDeleteAt> res = cardCommandService
                    .restoreCard((long) request.getCardId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteCardPermanent(FindByIdCardRequest request, StreamObserver<ApiResponseCardDelete> observer) {
        try {
            ApiResponse<Boolean> res = cardCommandService.deleteCard((long) request.getCardId());
            observer.onNext(ApiResponseCardDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllCard(Empty request, StreamObserver<ApiResponseCardAll> observer) {
        try {
            ApiResponse<Boolean> res = cardCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllCardPermanent(Empty request, StreamObserver<ApiResponseCardAll> observer) {
        try {
            ApiResponse<Boolean> res = cardCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponseCardDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.CardResponseDeleteAt> res) {
        ApiResponseCardDeleteAt.Builder builder = ApiResponseCardDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            builder.setData(toProtoDeleteAt(res.getData()));
        }
        return builder.build();
    }

    private ApiResponseCardAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseCardAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
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

    private java.time.LocalDate toLocalDate(com.google.protobuf.Timestamp ts) {
        if (ts == null || ts.getSeconds() == 0) {
            return null;
        }
        return java.time.Instant.ofEpochSecond(ts.getSeconds())
                .atZone(java.time.ZoneOffset.UTC)
                .toLocalDate();
    }
}
