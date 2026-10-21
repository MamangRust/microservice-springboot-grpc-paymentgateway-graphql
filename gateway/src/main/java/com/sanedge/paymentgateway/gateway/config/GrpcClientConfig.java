package com.sanedge.paymentgateway.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.paymentgateway.pb.AuthServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardDashboardServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.MerchantCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.MerchantTransactionServiceGrpc;
import com.sanedge.paymentgateway.pb.role.RoleCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.role.RoleServiceGrpc;
import com.sanedge.paymentgateway.pb.saldo.SaldoCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.saldo.SaldoQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.stats.StatsReaderServiceGrpc;
import com.sanedge.paymentgateway.pb.topup.TopupCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.topup.TopupQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.transaction.TransactionCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.transaction.TransactionQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.transfer.TransferCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.transfer.TransferQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.user.UserCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.user.UserQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

/**
 * One plaintext {@link ManagedChannel} per downstream domain service plus the
 * blocking stubs used by the REST controllers.
 */
@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.role.host:localhost}")
    private String roleHost;
    @Value("${grpc.client.role.port:9006}")
    private int rolePort;

    @Value("${grpc.client.user.host:localhost}")
    private String userHost;
    @Value("${grpc.client.user.port:9011}")
    private int userPort;

    @Value("${grpc.client.merchant.host:localhost}")
    private String merchantHost;
    @Value("${grpc.client.merchant.port:9005}")
    private int merchantPort;

    @Value("${grpc.client.card.host:localhost}")
    private String cardHost;
    @Value("${grpc.client.card.port:9004}")
    private int cardPort;

    @Value("${grpc.client.saldo.host:localhost}")
    private String saldoHost;
    @Value("${grpc.client.saldo.port:9007}")
    private int saldoPort;

    @Value("${grpc.client.topup.host:localhost}")
    private String topupHost;
    @Value("${grpc.client.topup.port:9008}")
    private int topupPort;

    @Value("${grpc.client.transaction.host:localhost}")
    private String transactionHost;
    @Value("${grpc.client.transaction.port:9009}")
    private int transactionPort;

    @Value("${grpc.client.transfer.host:localhost}")
    private String transferHost;
    @Value("${grpc.client.transfer.port:9010}")
    private int transferPort;

    @Value("${grpc.client.withdraw.host:localhost}")
    private String withdrawHost;
    @Value("${grpc.client.withdraw.port:9013}")
    private int withdrawPort;

    @Value("${grpc.client.auth.host:localhost}")
    private String authHost;
    @Value("${grpc.client.auth.port:9012}")
    private int authPort;

    @Value("${grpc.client.stats.host:localhost}")
    private String statsHost;
    @Value("${grpc.client.stats.port:9014}")
    private int statsPort;

    private ManagedChannel channel(String host, int port) {
        return NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    // --- channels ---
    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel roleChannel() {
        return channel(roleHost, rolePort);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel userChannel() {
        return channel(userHost, userPort);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel merchantChannel() {
        return channel(merchantHost, merchantPort);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel cardChannel() {
        return channel(cardHost, cardPort);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel saldoChannel() {
        return channel(saldoHost, saldoPort);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel topupChannel() {
        return channel(topupHost, topupPort);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel transactionChannel() {
        return channel(transactionHost, transactionPort);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel transferChannel() {
        return channel(transferHost, transferPort);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel withdrawChannel() {
        return channel(withdrawHost, withdrawPort);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel authChannel() {
        return channel(authHost, authPort);
    }

    // --- auth ---
    @Bean
    public AuthServiceGrpc.AuthServiceBlockingStub authStub(ManagedChannel authChannel) {
        return AuthServiceGrpc.newBlockingStub(authChannel);
    }

    // --- role ---
    @Bean
    public RoleServiceGrpc.RoleServiceBlockingStub roleQueryStub(ManagedChannel roleChannel) {
        return RoleServiceGrpc.newBlockingStub(roleChannel);
    }

    @Bean
    public RoleCommandServiceGrpc.RoleCommandServiceBlockingStub roleCommandStub(ManagedChannel roleChannel) {
        return RoleCommandServiceGrpc.newBlockingStub(roleChannel);
    }

    // --- user ---
    @Bean
    public UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub(ManagedChannel userChannel) {
        return UserQueryServiceGrpc.newBlockingStub(userChannel);
    }

    @Bean
    public UserCommandServiceGrpc.UserCommandServiceBlockingStub userCommandStub(ManagedChannel userChannel) {
        return UserCommandServiceGrpc.newBlockingStub(userChannel);
    }

    // --- merchant ---
    @Bean
    public MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub(ManagedChannel merchantChannel) {
        return MerchantQueryServiceGrpc.newBlockingStub(merchantChannel);
    }

    @Bean
    public MerchantCommandServiceGrpc.MerchantCommandServiceBlockingStub merchantCommandStub(
            ManagedChannel merchantChannel) {
        return MerchantCommandServiceGrpc.newBlockingStub(merchantChannel);
    }

    @Bean
    public MerchantTransactionServiceGrpc.MerchantTransactionServiceBlockingStub merchantTransactionStub(
            ManagedChannel merchantChannel) {
        return MerchantTransactionServiceGrpc.newBlockingStub(merchantChannel);
    }

    // --- card ---
    @Bean
    public CardQueryServiceGrpc.CardQueryServiceBlockingStub cardQueryStub(ManagedChannel cardChannel) {
        return CardQueryServiceGrpc.newBlockingStub(cardChannel);
    }

    @Bean
    public CardCommandServiceGrpc.CardCommandServiceBlockingStub cardCommandStub(ManagedChannel cardChannel) {
        return CardCommandServiceGrpc.newBlockingStub(cardChannel);
    }

    @Bean
    public CardDashboardServiceGrpc.CardDashboardServiceBlockingStub cardDashboardStub(ManagedChannel cardChannel) {
        return CardDashboardServiceGrpc.newBlockingStub(cardChannel);
    }

    // --- saldo ---
    @Bean
    public SaldoQueryServiceGrpc.SaldoQueryServiceBlockingStub saldoQueryStub(ManagedChannel saldoChannel) {
        return SaldoQueryServiceGrpc.newBlockingStub(saldoChannel);
    }

    @Bean
    public SaldoCommandServiceGrpc.SaldoCommandServiceBlockingStub saldoCommandStub(ManagedChannel saldoChannel) {
        return SaldoCommandServiceGrpc.newBlockingStub(saldoChannel);
    }

    // --- topup ---
    @Bean
    public TopupQueryServiceGrpc.TopupQueryServiceBlockingStub topupQueryStub(ManagedChannel topupChannel) {
        return TopupQueryServiceGrpc.newBlockingStub(topupChannel);
    }

    @Bean
    public TopupCommandServiceGrpc.TopupCommandServiceBlockingStub topupCommandStub(ManagedChannel topupChannel) {
        return TopupCommandServiceGrpc.newBlockingStub(topupChannel);
    }

    // --- transaction ---
    @Bean
    public TransactionQueryServiceGrpc.TransactionQueryServiceBlockingStub transactionQueryStub(
            ManagedChannel transactionChannel) {
        return TransactionQueryServiceGrpc.newBlockingStub(transactionChannel);
    }

    @Bean
    public TransactionCommandServiceGrpc.TransactionCommandServiceBlockingStub transactionCommandStub(
            ManagedChannel transactionChannel) {
        return TransactionCommandServiceGrpc.newBlockingStub(transactionChannel);
    }

    // --- transfer ---
    @Bean
    public TransferQueryServiceGrpc.TransferQueryServiceBlockingStub transferQueryStub(ManagedChannel transferChannel) {
        return TransferQueryServiceGrpc.newBlockingStub(transferChannel);
    }

    @Bean
    public TransferCommandServiceGrpc.TransferCommandServiceBlockingStub transferCommandStub(
            ManagedChannel transferChannel) {
        return TransferCommandServiceGrpc.newBlockingStub(transferChannel);
    }

    // --- withdraw ---
    @Bean
    public WithdrawQueryServiceGrpc.WithdrawQueryServiceBlockingStub withdrawQueryStub(ManagedChannel withdrawChannel) {
        return WithdrawQueryServiceGrpc.newBlockingStub(withdrawChannel);
    }

    @Bean
    public WithdrawCommandServiceGrpc.WithdrawCommandServiceBlockingStub withdrawCommandStub(
            ManagedChannel withdrawChannel) {
        return WithdrawCommandServiceGrpc.newBlockingStub(withdrawChannel);
    }

    // --- stats (ClickHouse analytics reader) ---
    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel statsChannel() {
        return channel(statsHost, statsPort);
    }

    @Bean
    public StatsReaderServiceGrpc.StatsReaderServiceBlockingStub statsReaderStub(ManagedChannel statsChannel) {
        return StatsReaderServiceGrpc.newBlockingStub(statsChannel);
    }
}
