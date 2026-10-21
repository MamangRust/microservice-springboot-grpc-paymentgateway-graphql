package com.sanedge.paymentgateway.card.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.card.ApiResponseDashboardCard;
import com.sanedge.paymentgateway.pb.card.ApiResponseDashboardCardNumber;
import com.sanedge.paymentgateway.pb.card.CardDashboardServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardResponseDashboard;
import com.sanedge.paymentgateway.pb.card.CardResponseDashboardCardNumber;
import com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest;
import com.sanedge.paymentgateway.card.service.CardDashboardService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardDashboardHandler extends CardDashboardServiceGrpc.CardDashboardServiceImplBase {

    private final CardDashboardService cardDashboardService;

    @Override
    public void dashboardCard(Empty request, StreamObserver<ApiResponseDashboardCard> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.dashboard.CardDashboard> res = cardDashboardService
                    .dashboard();

            ApiResponseDashboardCard.Builder builder = ApiResponseDashboardCard.newBuilder()
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
    public void dashboardCardNumber(FindByCardNumberRequest request,
            StreamObserver<ApiResponseDashboardCardNumber> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.card.domain.responses.dashboard.CardDashboardCard> res = cardDashboardService
                    .dashboardByCard(request.getCardNumber());

            ApiResponseDashboardCardNumber.Builder builder = ApiResponseDashboardCardNumber.newBuilder()
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

    private CardResponseDashboard toProto(
            com.sanedge.paymentgateway.card.domain.responses.dashboard.CardDashboard d) {
        return CardResponseDashboard.newBuilder()
                .setTotalBalance(v(d.getTotalBalance()))
                .setTotalTopup(v(d.getTotalTopup()))
                .setTotalWithdraw(v(d.getTotalWithdraw()))
                .setTotalTransaction(v(d.getTotalTransaction()))
                .setTotalTransfer(v(d.getTotalTransfer()))
                .build();
    }

    private CardResponseDashboardCardNumber toProto(
            com.sanedge.paymentgateway.card.domain.responses.dashboard.CardDashboardCard d) {
        return CardResponseDashboardCardNumber.newBuilder()
                .setTotalBalance(v(d.getTotalBalance()))
                .setTotalTopup(v(d.getTotalTopup()))
                .setTotalWithdraw(v(d.getTotalWithdraw()))
                .setTotalTransaction(v(d.getTotalTransaction()))
                .setTotalTransferSend(v(d.getTotalTransferSend()))
                .setTotalTransferReceiver(v(d.getTotalTransferReceiver()))
                .build();
    }

    private long v(Long value) {
        return value == null ? 0L : value;
    }
}
