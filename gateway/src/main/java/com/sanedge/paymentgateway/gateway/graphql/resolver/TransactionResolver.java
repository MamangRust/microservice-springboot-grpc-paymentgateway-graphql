package com.sanedge.paymentgateway.gateway.graphql.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.TransactionResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.TransactionResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.graphql.dto.Page;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.transaction.TransactionCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.transaction.TransactionQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TransactionResolver {

    private final TransactionQueryServiceGrpc.TransactionQueryServiceBlockingStub transactionQueryStub;
    private final TransactionCommandServiceGrpc.TransactionCommandServiceBlockingStub transactionCommandStub;

    // ---- CRUD ----

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TransactionResponse> transactions(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = transactionQueryStub.findAllTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindAllTransactionRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.transactions(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public TransactionResponse transaction(@Argument Long id) {
        var resp = transactionQueryStub.findByIdTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(Gql.i(id)).build());
        return ProtoMapper.transaction(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TransactionResponse> transactionsByCard(@Argument String cardNumber, @Argument Integer page,
            @Argument Integer pageSize, @Argument String search) {
        var resp = transactionQueryStub.findAllTransactionByCardNumber(
                com.sanedge.paymentgateway.pb.transaction.FindAllTransactionCardNumberRequest.newBuilder()
                        .setCardNumber(Gql.s(cardNumber))
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.transactions(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public List<TransactionResponse> transactionsByMerchant(@Argument Long merchantId) {
        var resp = transactionQueryStub.findTransactionByMerchantId(
                com.sanedge.paymentgateway.pb.transaction.FindTransactionByMerchantIdRequest.newBuilder()
                        .setMerchantId(Gql.i(merchantId)).build());
        return ProtoMapper.transactions(resp.getDataList());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TransactionResponseDeleteAt> activeTransactions(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = transactionQueryStub.findByActiveTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindAllTransactionRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.transactionsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<TransactionResponseDeleteAt> trashedTransactions(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = transactionQueryStub.findByTrashedTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindAllTransactionRequest.newBuilder()
                        .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.transactionsDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public TransactionResponse createTransaction(@Argument("input") CreateTransaction input,
            @Argument String apiKey) {
        var resp = transactionCommandStub.createTransaction(
                com.sanedge.paymentgateway.pb.transaction.CreateTransactionRequest.newBuilder()
                        .setApiKey(Gql.s(apiKey))
                        .setCardNumber(Gql.s(input.cardNumber()))
                        .setAmount(Gql.i(input.amount()))
                        .setPaymentMethod(Gql.s(input.paymentMethod()))
                        .setMerchantId(Gql.i(input.merchantId()))
                        .build());
        return ProtoMapper.transaction(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public TransactionResponse updateTransaction(@Argument("input") UpdateTransaction input,
            @Argument String apiKey) {
        var resp = transactionCommandStub.updateTransaction(
                com.sanedge.paymentgateway.pb.transaction.UpdateTransactionRequest.newBuilder()
                        .setTransactionId(Gql.i(input.transactionId()))
                        .setApiKey(Gql.s(apiKey))
                        .setCardNumber(Gql.s(input.cardNumber()))
                        .setAmount(Gql.i(input.amount()))
                        .setPaymentMethod(Gql.s(input.paymentMethod()))
                        .setMerchantId(Gql.i(input.merchantId()))
                        .build());
        return ProtoMapper.transaction(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public TransactionResponseDeleteAt trashTransaction(@Argument Long id) {
        var resp = transactionCommandStub.trashedTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(Gql.i(id)).build());
        return ProtoMapper.transactionDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public TransactionResponseDeleteAt restoreTransaction(@Argument Long id) {
        var resp = transactionCommandStub.restoreTransaction(
                com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(Gql.i(id)).build());
        return ProtoMapper.transactionDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteTransactionPermanent(@Argument Long id) {
        var resp = transactionCommandStub.deleteTransactionPermanent(
                com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(Gql.i(id)).build());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean restoreAllTransactions() {
        var resp = transactionCommandStub.restoreAllTransaction(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteAllTransactionsPermanent() {
        var resp = transactionCommandStub.deleteAllTransactionPermanent(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    public record CreateTransaction(String cardNumber, Double amount, String paymentMethod, Long merchantId) {
    }

    public record UpdateTransaction(Long transactionId, String cardNumber, Double amount, String paymentMethod,
            Long merchantId) {
    }
}
