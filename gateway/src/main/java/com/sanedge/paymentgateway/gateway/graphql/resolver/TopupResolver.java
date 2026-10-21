package com.sanedge.paymentgateway.gateway.graphql.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.TopupResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.TopupResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.graphql.dto.Page;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.topup.TopupCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.topup.TopupQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TopupResolver {

    private final TopupQueryServiceGrpc.TopupQueryServiceBlockingStub topupQueryStub;
    private final TopupCommandServiceGrpc.TopupCommandServiceBlockingStub topupCommandStub;

    // ---- CRUD ----

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TopupResponse> topups(@Argument Integer page, @Argument Integer pageSize, @Argument String search) {
        var resp = topupQueryStub.findAllTopup(com.sanedge.paymentgateway.pb.topup.FindAllTopupRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.topups(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public TopupResponse topup(@Argument Long id) {
        var resp = topupQueryStub.findByIdTopup(com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest.newBuilder()
                .setTopupId(Gql.i(id)).build());
        return ProtoMapper.topup(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TopupResponse> topupsByCard(@Argument String cardNumber, @Argument Integer page,
            @Argument Integer pageSize, @Argument String search) {
        var resp = topupQueryStub.findAllTopupByCardNumber(
                com.sanedge.paymentgateway.pb.topup.FindAllTopupByCardNumberRequest.newBuilder()
                        .setCardNumber(Gql.s(cardNumber))
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.topups(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public List<TopupResponse> topupsOfCard(@Argument String cardNumber) {
        var resp = topupQueryStub.findByCardNumberTopup(
                com.sanedge.paymentgateway.pb.topup.FindByCardNumberTopupRequest.newBuilder()
                        .setCardNumber(Gql.s(cardNumber)).build());
        return resp.hasData() ? List.of(ProtoMapper.topup(resp.getData())) : List.of();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TopupResponseDeleteAt> activeTopups(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = topupQueryStub.findByActive(com.sanedge.paymentgateway.pb.topup.FindAllTopupRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.topupsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TopupResponseDeleteAt> trashedTopups(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = topupQueryStub.findByTrashed(com.sanedge.paymentgateway.pb.topup.FindAllTopupRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.topupsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public TopupResponse createTopup(@Argument("input") CreateTopup input) {
        var resp = topupCommandStub.createTopup(com.sanedge.paymentgateway.pb.topup.CreateTopupRequest.newBuilder()
                .setCardNumber(Gql.s(input.cardNumber()))
                .setTopupNo("")
                .setTopupAmount(Gql.i(input.topupAmount()))
                .setTopupMethod(Gql.s(input.topupMethod())).build());
        return ProtoMapper.topup(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public TopupResponse updateTopup(@Argument("input") UpdateTopup input) {
        var resp = topupCommandStub.updateTopup(com.sanedge.paymentgateway.pb.topup.UpdateTopupRequest.newBuilder()
                .setTopupId(Gql.i(input.topupId()))
                .setCardNumber(Gql.s(input.cardNumber()))
                .setTopupAmount(Gql.i(input.topupAmount()))
                .setTopupMethod(Gql.s(input.topupMethod())).build());
        return ProtoMapper.topup(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public TopupResponseDeleteAt trashTopup(@Argument Long id) {
        var resp = topupCommandStub.trashedTopup(
                com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest.newBuilder()
                        .setTopupId(Gql.i(id)).build());
        return ProtoMapper.topupDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public TopupResponseDeleteAt restoreTopup(@Argument Long id) {
        var resp = topupCommandStub.restoreTopup(
                com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest.newBuilder()
                        .setTopupId(Gql.i(id)).build());
        return ProtoMapper.topupDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteTopupPermanent(@Argument Long id) {
        var resp = topupCommandStub.deleteTopupPermanent(
                com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest.newBuilder()
                        .setTopupId(Gql.i(id)).build());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean restoreAllTopups() {
        var resp = topupCommandStub.restoreAllTopup(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteAllTopupsPermanent() {
        var resp = topupCommandStub.deleteAllTopupPermanent(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    public record CreateTopup(String cardNumber, Double topupAmount, String topupMethod) {
    }

    public record UpdateTopup(Long topupId, String cardNumber, Double topupAmount, String topupMethod) {
    }
}
