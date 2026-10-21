package com.sanedge.paymentgateway.common.adapter.card;

import java.time.Instant;

import com.google.protobuf.Timestamp;
import com.sanedge.paymentgateway.common.adapter.model.Card;
import com.sanedge.paymentgateway.common.adapter.support.ProtoTime;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.pb.card.CardCommandServiceGrpc.CardCommandServiceBlockingStub;
import com.sanedge.paymentgateway.pb.card.CardQueryServiceGrpc.CardQueryServiceBlockingStub;
import com.sanedge.paymentgateway.pb.card.CardResponse;
import com.sanedge.paymentgateway.pb.card.CardWithEmailResponse;
import com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest;
import com.sanedge.paymentgateway.pb.card.FindByUserIdCardRequest;
import com.sanedge.paymentgateway.pb.card.UpdateCardRequest;

public class CardAdapter implements CardPort {

    private final CardQueryServiceBlockingStub query;

    private final CardCommandServiceBlockingStub command;

    public CardAdapter(CardQueryServiceBlockingStub query, CardCommandServiceBlockingStub command) {
        this.query = query;
        this.command = command;
    }

    @Override
    public Card findCardByUserId(int userId) {
        var resp = query.findByUserIdCard(FindByUserIdCardRequest.newBuilder().setUserId(userId).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Card not found for user: " + userId);
        }
        return toCard(resp.getData());
    }

    @Override
    public Card findUserCardByCardNumber(String cardNumber) {
        var resp = query.findUserCardByCardNumber(FindByCardNumberRequest.newBuilder().setCardNumber(cardNumber).build());
        if (resp == null || resp.getCardNumber() == null || resp.getCardNumber().isEmpty()) {
            throw new ResourceNotFoundException("Card not found: " + cardNumber);
        }
        return toCard(resp);
    }

    @Override
    public Card findCardByCardNumber(String cardNumber) {
        var resp = query.findByCardNumber(FindByCardNumberRequest.newBuilder().setCardNumber(cardNumber).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Card not found: " + cardNumber);
        }
        return toCard(resp.getData());
    }

    @Override
    public Card updateCard(UpdateData data) {
        UpdateCardRequest.Builder builder = UpdateCardRequest.newBuilder()
                .setCardId(data.cardId())
                .setUserId(data.userId())
                .setCardType(nullSafe(data.cardType()))
                .setCvv(nullSafe(data.cvv()))
                .setCardProvider(nullSafe(data.cardProvider()));
        Instant expire = ProtoTime.parse(data.expireDate());
        if (expire != null) {
            builder.setExpireDate(Timestamp.newBuilder().setSeconds(expire.getEpochSecond()).build());
        }
        var resp = command.updateCard(builder.build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Card not found: " + data.cardId());
        }
        return toCard(resp.getData());
    }

    private static Card toCard(CardResponse r) {
        if (r == null) {
            return null;
        }
        return new Card(r.getId(), r.getUserId(), r.getCardNumber(), r.getCardType(), r.getExpireDate(),
                r.getCvv(), r.getCardProvider(), null,
                ProtoTime.parse(r.getCreatedAt()), ProtoTime.parse(r.getUpdatedAt()));
    }

    private static Card toCard(CardWithEmailResponse r) {
        if (r == null) {
            return null;
        }
        return new Card(r.getId(), r.getUserId(), r.getCardNumber(), r.getCardType(), r.getExpireDate(),
                r.getCvv(), r.getCardProvider(), r.getEmail(),
                ProtoTime.parse(r.getCreatedAt()), ProtoTime.parse(r.getUpdatedAt()));
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
