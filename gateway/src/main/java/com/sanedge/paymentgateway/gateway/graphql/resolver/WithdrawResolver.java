package com.sanedge.paymentgateway.gateway.graphql.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.WithdrawResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.WithdrawResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.graphql.dto.Page;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WithdrawResolver {

    private final WithdrawQueryServiceGrpc.WithdrawQueryServiceBlockingStub withdrawQueryStub;
    private final WithdrawCommandServiceGrpc.WithdrawCommandServiceBlockingStub withdrawCommandStub;

    // ---- CRUD ----

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<WithdrawResponse> withdraws(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = withdrawQueryStub.findAllWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.FindAllWithdrawRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.withdraws(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public WithdrawResponse withdraw(@Argument Long id) {
        var resp = withdrawQueryStub.findByIdWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(Gql.i(id)).build());
        return ProtoMapper.withdraw(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public List<WithdrawResponse> withdrawsByCard(@Argument String cardNumber) {
        var resp = withdrawQueryStub.findByCardNumber(
                com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest.newBuilder()
                        .setCardNumber(Gql.s(cardNumber)).build());
        return ProtoMapper.withdraws(resp.getDataList());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<WithdrawResponseDeleteAt> activeWithdraws(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = withdrawQueryStub.findByActive(
                com.sanedge.paymentgateway.pb.withdraw.FindAllWithdrawRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.withdrawsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<WithdrawResponseDeleteAt> trashedWithdraws(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = withdrawQueryStub.findByTrashed(
                com.sanedge.paymentgateway.pb.withdraw.FindAllWithdrawRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.withdrawsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WithdrawResponse createWithdraw(@Argument("input") CreateWithdraw input) {
        var resp = withdrawCommandStub.createWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.CreateWithdrawRequest.newBuilder()
                        .setCardNumber(Gql.s(input.cardNumber()))
                        .setWithdrawAmount(Gql.i(input.withdrawAmount())).build());
        return ProtoMapper.withdraw(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WithdrawResponse updateWithdraw(@Argument("input") UpdateWithdraw input) {
        var resp = withdrawCommandStub.updateWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.UpdateWithdrawRequest.newBuilder()
                        .setWithdrawId(Gql.i(input.withdrawId()))
                        .setCardNumber(Gql.s(input.cardNumber()))
                        .setWithdrawAmount(Gql.i(input.withdrawAmount())).build());
        return ProtoMapper.withdraw(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WithdrawResponseDeleteAt trashWithdraw(@Argument Long id) {
        var resp = withdrawCommandStub.trashedWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(Gql.i(id)).build());
        return ProtoMapper.withdrawDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WithdrawResponseDeleteAt restoreWithdraw(@Argument Long id) {
        var resp = withdrawCommandStub.restoreWithdraw(
                com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(Gql.i(id)).build());
        return ProtoMapper.withdrawDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteWithdrawPermanent(@Argument Long id) {
        var resp = withdrawCommandStub.deleteWithdrawPermanent(
                com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(Gql.i(id)).build());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean restoreAllWithdraws() {
        var resp = withdrawCommandStub.restoreAllWithdraw(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteAllWithdrawsPermanent() {
        var resp = withdrawCommandStub.deleteAllWithdrawPermanent(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    public record CreateWithdraw(String cardNumber, Double withdrawAmount) {
    }

    public record UpdateWithdraw(Long withdrawId, String cardNumber, Double withdrawAmount) {
    }
}
