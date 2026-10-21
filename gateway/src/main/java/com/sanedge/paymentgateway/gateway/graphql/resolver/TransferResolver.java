package com.sanedge.paymentgateway.gateway.graphql.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.TransferResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.TransferResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.graphql.dto.Page;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.transfer.TransferCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.transfer.TransferQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TransferResolver {

    private final TransferQueryServiceGrpc.TransferQueryServiceBlockingStub transferQueryStub;
    private final TransferCommandServiceGrpc.TransferCommandServiceBlockingStub transferCommandStub;

    // ---- CRUD ----

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TransferResponse> transfers(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = transferQueryStub.findAllTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindAllTransferRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.transfers(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public TransferResponse transfer(@Argument Long id) {
        var resp = transferQueryStub.findByIdTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(Gql.i(id)).build());
        return ProtoMapper.transfer(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TransferResponseDeleteAt> activeTransfers(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = transferQueryStub.findByActiveTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindAllTransferRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.transfersDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TransferResponseDeleteAt> trashedTransfers(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = transferQueryStub.findByTrashedTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindAllTransferRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.transfersDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public List<TransferResponse> transfersFrom(@Argument String transferFrom) {
        var resp = transferQueryStub.findTransferByTransferFrom(
                com.sanedge.paymentgateway.pb.transfer.FindTransferByTransferFromRequest.newBuilder()
                        .setTransferFrom(Gql.s(transferFrom)).build());
        return ProtoMapper.transfers(resp.getDataList());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public List<TransferResponse> transfersTo(@Argument String transferTo) {
        var resp = transferQueryStub.findTransferByTransferTo(
                com.sanedge.paymentgateway.pb.transfer.FindTransferByTransferToRequest.newBuilder()
                        .setTransferTo(Gql.s(transferTo)).build());
        return ProtoMapper.transfers(resp.getDataList());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public TransferResponse createTransfer(@Argument("input") CreateTransfer input) {
        var resp = transferCommandStub.createTransfer(
                com.sanedge.paymentgateway.pb.transfer.CreateTransferRequest.newBuilder()
                        .setTransferFrom(Gql.s(input.transferFrom()))
                        .setTransferTo(Gql.s(input.transferTo()))
                        .setTransferAmount(Gql.i(input.transferAmount())).build());
        return ProtoMapper.transfer(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public TransferResponse updateTransfer(@Argument("input") UpdateTransfer input) {
        var resp = transferCommandStub.updateTransfer(
                com.sanedge.paymentgateway.pb.transfer.UpdateTransferRequest.newBuilder()
                        .setTransferId(Gql.i(input.transferId()))
                        .setTransferFrom(Gql.s(input.transferFrom()))
                        .setTransferTo(Gql.s(input.transferTo()))
                        .setTransferAmount(Gql.i(input.transferAmount())).build());
        return ProtoMapper.transfer(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public TransferResponseDeleteAt trashTransfer(@Argument Long id) {
        var resp = transferCommandStub.trashedTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(Gql.i(id)).build());
        return ProtoMapper.transferDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public TransferResponseDeleteAt restoreTransfer(@Argument Long id) {
        var resp = transferCommandStub.restoreTransfer(
                com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(Gql.i(id)).build());
        return ProtoMapper.transferDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Boolean deleteTransferPermanent(@Argument Long id) {
        var resp = transferCommandStub.deleteTransferPermanent(
                com.sanedge.paymentgateway.pb.transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(Gql.i(id)).build());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean restoreAllTransfers() {
        var resp = transferCommandStub.restoreAllTransfer(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteAllTransfersPermanent() {
        var resp = transferCommandStub.deleteAllTransferPermanent(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    public record CreateTransfer(String transferFrom, String transferTo, Double transferAmount) {
    }

    public record UpdateTransfer(Long transferId, String transferFrom, String transferTo, Double transferAmount) {
    }
}
