package com.sanedge.paymentgateway.gateway.graphql.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.MerchantResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.MerchantResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.MerchantTransactionResponse;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.graphql.dto.Page;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.merchant.MerchantCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.MerchantTransactionServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MerchantResolver {

    private final MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub;
    private final MerchantCommandServiceGrpc.MerchantCommandServiceBlockingStub merchantCommandStub;
    private final MerchantTransactionServiceGrpc.MerchantTransactionServiceBlockingStub merchantTransactionStub;

    // ---- CRUD ----

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<MerchantResponse> merchants(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = merchantQueryStub.findAllMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindAllMerchantRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.merchants(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public MerchantResponse merchant(@Argument Long id) {
        var resp = merchantQueryStub.findByIdMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(Gql.i(id)).build());
        return ProtoMapper.merchant(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<MerchantResponseDeleteAt> activeMerchants(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = merchantQueryStub.findByActive(
                com.sanedge.paymentgateway.pb.merchant.FindAllMerchantRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.merchantsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<MerchantResponseDeleteAt> trashedMerchants(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = merchantQueryStub.findByTrashed(
                com.sanedge.paymentgateway.pb.merchant.FindAllMerchantRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.merchantsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public MerchantResponse merchantByApiKey(@Argument String apiKey) {
        var resp = merchantQueryStub.findByApiKey(
                com.sanedge.paymentgateway.pb.merchant.FindByApiKeyRequest.newBuilder()
                        .setApiKey(Gql.s(apiKey)).build());
        return ProtoMapper.merchant(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public List<MerchantResponse> merchantsByUser(@Argument Long userId) {
        var resp = merchantQueryStub.findByMerchantUserId(
                com.sanedge.paymentgateway.pb.merchant.FindByMerchantUserIdRequest.newBuilder()
                        .setUserId(Gql.i(userId)).build());
        return ProtoMapper.merchants(resp.getDataList());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public MerchantResponse createMerchant(@Argument("input") CreateMerchant input) {
        var resp = merchantCommandStub.createMerchant(
                com.sanedge.paymentgateway.pb.merchant.CreateMerchantRequest.newBuilder()
                        .setName(Gql.s(input.name()))
                        .setUserId(Gql.i(input.userId())).build());
        return ProtoMapper.merchant(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public MerchantResponse updateMerchant(@Argument("input") UpdateMerchant input) {
        var resp = merchantCommandStub.updateMerchant(
                com.sanedge.paymentgateway.pb.merchant.UpdateMerchantRequest.newBuilder()
                        .setMerchantId(Gql.i(input.merchantId()))
                        .setName(Gql.s(input.name()))
                        .setUserId(Gql.i(input.userId()))
                        .setStatus(Gql.s(input.status())).build());
        return ProtoMapper.merchant(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public MerchantResponseDeleteAt trashMerchant(@Argument Long id) {
        var resp = merchantCommandStub.trashedMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(Gql.i(id)).build());
        return ProtoMapper.merchantDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public MerchantResponseDeleteAt restoreMerchant(@Argument Long id) {
        var resp = merchantCommandStub.restoreMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(Gql.i(id)).build());
        return ProtoMapper.merchantDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteMerchantPermanent(@Argument Long id) {
        var resp = merchantCommandStub.deleteMerchantPermanent(
                com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(Gql.i(id)).build());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean restoreAllMerchants() {
        var resp = merchantCommandStub.restoreAllMerchant(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteAllMerchantsPermanent() {
        var resp = merchantCommandStub.deleteAllMerchantPermanent(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    // ---- transactions ----

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<MerchantTransactionResponse> merchantTransactions(@Argument Integer page,
            @Argument Integer pageSize, @Argument String search) {
        var resp = merchantTransactionStub.findAllTransactionMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindAllMerchantTransaction.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.merchantTransactions(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<MerchantTransactionResponse> merchantTransactionsByMerchant(@Argument Long merchantId,
            @Argument Integer page, @Argument Integer pageSize, @Argument String search) {
        var resp = merchantTransactionStub.findAllTransactionByMerchant(
                com.sanedge.paymentgateway.pb.merchant.FindAllMerchantTransactionId.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search))
                        .setId(merchantId == null ? "" : String.valueOf(merchantId)).build());
        return Page.of(ProtoMapper.merchantTransactions(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<MerchantTransactionResponse> merchantTransactionsByApiKey(@Argument String apiKey,
            @Argument Integer page, @Argument Integer pageSize, @Argument String search) {
        var resp = merchantTransactionStub.findAllTransactionByApikey(
                com.sanedge.paymentgateway.pb.merchant.FindAllMerchantTransactionApikey.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search))
                        .setApiKey(Gql.s(apiKey)).build());
        return Page.of(ProtoMapper.merchantTransactions(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    public record CreateMerchant(String name, Long userId) {
    }

    public record UpdateMerchant(Long merchantId, String name, Long userId, String status) {
    }
}
