package com.sanedge.paymentgateway.gateway.graphql.resolver;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.responses.card.CardResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.card.CardResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.card.dashboard.CardDashboard;
import com.sanedge.paymentgateway.gateway.dto.responses.card.dashboard.CardDashboardCard;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.graphql.dto.Page;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.card.CardCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardDashboardServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CardResolver {

    private final CardQueryServiceGrpc.CardQueryServiceBlockingStub cardQueryStub;
    private final CardCommandServiceGrpc.CardCommandServiceBlockingStub cardCommandStub;
    private final CardDashboardServiceGrpc.CardDashboardServiceBlockingStub cardDashboardStub;

    // ---- CRUD ----

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<CardResponse> cards(@Argument Integer page, @Argument Integer pageSize, @Argument String search) {
        var resp = cardQueryStub.findAllCard(com.sanedge.paymentgateway.pb.card.FindAllCardRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.cards(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public CardResponse card(@Argument Long id) {
        var resp = cardQueryStub.findByIdCard(com.sanedge.paymentgateway.pb.card.FindByIdCardRequest.newBuilder()
                .setCardId(Gql.i(id)).build());
        return ProtoMapper.card(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<CardResponseDeleteAt> activeCards(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = cardQueryStub.findByActiveCard(com.sanedge.paymentgateway.pb.card.FindAllCardRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.cardsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Page<CardResponseDeleteAt> trashedCards(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = cardQueryStub.findByTrashedCard(com.sanedge.paymentgateway.pb.card.FindAllCardRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.cardsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public CardResponse cardByUser(@Argument Long userId) {
        var resp = cardQueryStub.findByUserIdCard(
                com.sanedge.paymentgateway.pb.card.FindByUserIdCardRequest.newBuilder()
                        .setUserId(Gql.i(userId)).build());
        return ProtoMapper.card(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public CardResponse cardByNumber(@Argument String cardNumber) {
        var resp = cardQueryStub.findByCardNumber(
                com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest.newBuilder()
                        .setCardNumber(Gql.s(cardNumber)).build());
        return ProtoMapper.card(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public CardResponse createCard(@Argument("input") CreateCard input) {
        var resp = cardCommandStub.createCard(com.sanedge.paymentgateway.pb.card.CreateCardRequest.newBuilder()
                .setUserId(Gql.i(input.userId()))
                .setCardType(Gql.s(input.cardType()))
                .setExpireDate(ProtoMapper.toTimestamp(Gql.date(input.expireDate())))
                .setCvv(Gql.s(input.cvv()))
                .setCardProvider(Gql.s(input.cardProvider())).build());
        return ProtoMapper.card(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public CardResponse updateCard(@Argument("input") UpdateCard input) {
        var resp = cardCommandStub.updateCard(com.sanedge.paymentgateway.pb.card.UpdateCardRequest.newBuilder()
                .setCardId(Gql.i(input.cardId()))
                .setUserId(Gql.i(input.userId()))
                .setCardType(Gql.s(input.cardType()))
                .setExpireDate(ProtoMapper.toTimestamp(Gql.date(input.expireDate())))
                .setCvv(Gql.s(input.cvv()))
                .setCardProvider(Gql.s(input.cardProvider())).build());
        return ProtoMapper.card(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public CardResponseDeleteAt trashCard(@Argument Long id) {
        var resp = cardCommandStub.trashedCard(
                com.sanedge.paymentgateway.pb.card.FindByIdCardRequest.newBuilder()
                        .setCardId(Gql.i(id)).build());
        return ProtoMapper.cardDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public CardResponseDeleteAt restoreCard(@Argument Long id) {
        var resp = cardCommandStub.restoreCard(
                com.sanedge.paymentgateway.pb.card.FindByIdCardRequest.newBuilder()
                        .setCardId(Gql.i(id)).build());
        return ProtoMapper.cardDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteCardPermanent(@Argument Long id) {
        var resp = cardCommandStub.deleteCardPermanent(
                com.sanedge.paymentgateway.pb.card.FindByIdCardRequest.newBuilder()
                        .setCardId(Gql.i(id)).build());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean restoreAllCards() {
        var resp = cardCommandStub.restoreAllCard(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteAllCardsPermanent() {
        var resp = cardCommandStub.deleteAllCardPermanent(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    // ---- dashboard ----

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public CardDashboard cardDashboard() {
        var resp = cardDashboardStub.dashboardCard(Empty.getDefaultInstance());
        return ProtoMapper.cardDashboard(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public CardDashboardCard cardDashboardByNumber(@Argument String cardNumber) {
        var resp = cardDashboardStub.dashboardCardNumber(
                com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest.newBuilder()
                        .setCardNumber(Gql.s(cardNumber)).build());
        return ProtoMapper.cardDashboardCard(resp.getData());
    }

    public record CreateCard(Long userId, String cardType, String expireDate, String cvv, String cardProvider) {
    }

    public record UpdateCard(Long cardId, Long userId, String cardType, String expireDate, String cvv,
            String cardProvider) {
    }
}
