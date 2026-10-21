package com.sanedge.paymentgateway.gateway.graphql.resolver;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.responses.saldo.SaldoResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.saldo.SaldoResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.graphql.dto.Page;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.saldo.SaldoCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.saldo.SaldoQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SaldoResolver {

    private final SaldoQueryServiceGrpc.SaldoQueryServiceBlockingStub saldoQueryStub;
    private final SaldoCommandServiceGrpc.SaldoCommandServiceBlockingStub saldoCommandStub;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<SaldoResponse> saldos(@Argument Integer page, @Argument Integer pageSize, @Argument String search) {
        var resp = saldoQueryStub.findAllSaldo(com.sanedge.paymentgateway.pb.saldo.FindAllSaldoRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.saldos(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public SaldoResponse saldo(@Argument Long id) {
        var resp = saldoQueryStub.findByIdSaldo(com.sanedge.paymentgateway.pb.saldo.FindByIdSaldoRequest.newBuilder()
                .setSaldoId(Gql.i(id)).build());
        return ProtoMapper.saldo(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<SaldoResponseDeleteAt> activeSaldos(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = saldoQueryStub.findByActive(com.sanedge.paymentgateway.pb.saldo.FindAllSaldoRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.saldosDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<SaldoResponseDeleteAt> trashedSaldos(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = saldoQueryStub.findByTrashed(com.sanedge.paymentgateway.pb.saldo.FindAllSaldoRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.saldosDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public SaldoResponse saldoByCard(@Argument String cardNumber) {
        var resp = saldoQueryStub.findByCardNumber(
                com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest.newBuilder()
                        .setCardNumber(Gql.s(cardNumber)).build());
        return ProtoMapper.saldo(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public SaldoResponse createSaldo(@Argument("input") CreateSaldo input) {
        var resp = saldoCommandStub.createSaldo(com.sanedge.paymentgateway.pb.saldo.CreateSaldoRequest.newBuilder()
                .setCardNumber(Gql.s(input.cardNumber()))
                .setTotalBalance(Gql.i(input.totalBalance())).build());
        return ProtoMapper.saldo(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public SaldoResponse updateSaldo(@Argument("input") UpdateSaldo input) {
        var resp = saldoCommandStub.updateSaldo(com.sanedge.paymentgateway.pb.saldo.UpdateSaldoRequest.newBuilder()
                .setSaldoId(Gql.i(input.saldoId()))
                .setCardNumber(Gql.s(input.cardNumber()))
                .setTotalBalance(Gql.i(input.totalBalance())).build());
        return ProtoMapper.saldo(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public SaldoResponseDeleteAt trashSaldo(@Argument Long id) {
        var resp = saldoCommandStub.trashedSaldo(
                com.sanedge.paymentgateway.pb.saldo.FindByIdSaldoRequest.newBuilder()
                        .setSaldoId(Gql.i(id)).build());
        return ProtoMapper.saldoDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public SaldoResponseDeleteAt restoreSaldo(@Argument Long id) {
        var resp = saldoCommandStub.restoreSaldo(
                com.sanedge.paymentgateway.pb.saldo.FindByIdSaldoRequest.newBuilder()
                        .setSaldoId(Gql.i(id)).build());
        return ProtoMapper.saldoDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteSaldoPermanent(@Argument Long id) {
        var resp = saldoCommandStub.deleteSaldoPermanent(
                com.sanedge.paymentgateway.pb.saldo.FindByIdSaldoRequest.newBuilder()
                        .setSaldoId(Gql.i(id)).build());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean restoreAllSaldos() {
        var resp = saldoCommandStub.restoreAllSaldo(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteAllSaldosPermanent() {
        var resp = saldoCommandStub.deleteAllSaldoPermanent(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    public record CreateSaldo(String cardNumber, Double totalBalance) {
    }

    public record UpdateSaldo(Long saldoId, String cardNumber, Double totalBalance) {
    }
}
