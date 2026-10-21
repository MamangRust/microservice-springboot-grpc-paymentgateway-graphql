package com.sanedge.paymentgateway.saldo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.paymentgateway.common.adapter.card.CardAdapter;
import com.sanedge.paymentgateway.common.adapter.card.CardPort;
import com.sanedge.paymentgateway.common.resilience.DependencyGuardInterceptor;
import com.sanedge.paymentgateway.pb.card.CardCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.card.CardQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel cardChannel(
            @Value("${grpc.client.card.host:localhost}") String host,
            @Value("${grpc.client.card.port:9004}") int port,
            DependencyGuardInterceptor dependencyGuardInterceptor) {
        return NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean
    public CardQueryServiceGrpc.CardQueryServiceBlockingStub cardQueryServiceBlockingStub(ManagedChannel cardChannel) {
        return CardQueryServiceGrpc.newBlockingStub(cardChannel);
    }

    @Bean
    public CardCommandServiceGrpc.CardCommandServiceBlockingStub cardCommandServiceBlockingStub(
            ManagedChannel cardChannel) {
        return CardCommandServiceGrpc.newBlockingStub(cardChannel);
    }

    @Bean
    public CardPort cardPort(CardQueryServiceGrpc.CardQueryServiceBlockingStub cardQueryServiceBlockingStub,
            CardCommandServiceGrpc.CardCommandServiceBlockingStub cardCommandServiceBlockingStub) {
        return new CardAdapter(cardQueryServiceBlockingStub, cardCommandServiceBlockingStub);
    }
}
