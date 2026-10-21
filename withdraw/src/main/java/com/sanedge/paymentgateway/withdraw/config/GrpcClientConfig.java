package com.sanedge.paymentgateway.withdraw.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.paymentgateway.common.adapter.card.CardAdapter;
import com.sanedge.paymentgateway.common.adapter.card.CardPort;
import com.sanedge.paymentgateway.common.adapter.fraud.FraudAdapter;
import com.sanedge.paymentgateway.common.adapter.fraud.FraudPort;
import com.sanedge.paymentgateway.common.adapter.saldo.SaldoAdapter;
import com.sanedge.paymentgateway.common.adapter.saldo.SaldoPort;
import com.sanedge.paymentgateway.common.resilience.DependencyGuardInterceptor;
import com.sanedge.paymentgateway.pb.ai_security.AISecurityServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.saldo.SaldoCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.saldo.SaldoQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;

/**
 * One plaintext {@link ManagedChannel} per downstream domain service plus the
 * blocking stubs used by the withdraw command flow (card lookup + saldo
 * balance/withdraw updates).
 */
@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.card.host:localhost}")
    private String cardHost;
    @Value("${grpc.client.card.port:9004}")
    private int cardPort;

    @Value("${grpc.client.saldo.host:localhost}")
    private String saldoHost;
    @Value("${grpc.client.saldo.port:9007}")
    private int saldoPort;

    @Value("${grpc.client.ai-security.host:localhost}")
    private String aiSecurityHost;
    @Value("${grpc.client.ai-security.port:50051}")
    private int aiSecurityPort;

    private final List<ManagedChannel> channels = new ArrayList<>();

    private final DependencyGuardInterceptor dependencyGuardInterceptor;

    public GrpcClientConfig(DependencyGuardInterceptor dependencyGuardInterceptor) {
        this.dependencyGuardInterceptor = dependencyGuardInterceptor;
    }

    private ManagedChannel channel(String host, int port) {
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
        channels.add(channel);
        return channel;
    }

    @Bean
    public CardQueryServiceGrpc.CardQueryServiceBlockingStub cardQueryStub() {
        return CardQueryServiceGrpc.newBlockingStub(channel(cardHost, cardPort));
    }

    @Bean
    public CardCommandServiceGrpc.CardCommandServiceBlockingStub cardCommandStub() {
        return CardCommandServiceGrpc.newBlockingStub(channel(cardHost, cardPort));
    }

    @Bean
    public SaldoQueryServiceGrpc.SaldoQueryServiceBlockingStub saldoQueryStub() {
        return SaldoQueryServiceGrpc.newBlockingStub(channel(saldoHost, saldoPort));
    }

    @Bean
    public SaldoCommandServiceGrpc.SaldoCommandServiceBlockingStub saldoCommandStub() {
        return SaldoCommandServiceGrpc.newBlockingStub(channel(saldoHost, saldoPort));
    }

    @Bean
    public AISecurityServiceGrpc.AISecurityServiceBlockingStub aiSecurityStub() {
        return AISecurityServiceGrpc.newBlockingStub(channel(aiSecurityHost, aiSecurityPort));
    }

    @Bean
    public CardPort cardPort(CardQueryServiceGrpc.CardQueryServiceBlockingStub cardQueryStub,
            CardCommandServiceGrpc.CardCommandServiceBlockingStub cardCommandStub) {
        return new CardAdapter(cardQueryStub, cardCommandStub);
    }

    @Bean
    public SaldoPort saldoPort(SaldoQueryServiceGrpc.SaldoQueryServiceBlockingStub saldoQueryStub,
            SaldoCommandServiceGrpc.SaldoCommandServiceBlockingStub saldoCommandStub) {
        return new SaldoAdapter(saldoQueryStub, saldoCommandStub);
    }

    @Bean
    public FraudPort fraudPort(AISecurityServiceGrpc.AISecurityServiceBlockingStub aiSecurityStub) {
        return new FraudAdapter(aiSecurityStub);
    }

    @PreDestroy
    public void shutdown() {
        for (ManagedChannel channel : channels) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
