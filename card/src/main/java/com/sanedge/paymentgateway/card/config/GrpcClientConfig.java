package com.sanedge.paymentgateway.card.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.paymentgateway.common.adapter.user.UserAdapter;
import com.sanedge.paymentgateway.common.adapter.user.UserPort;
import com.sanedge.paymentgateway.common.resilience.DependencyGuardInterceptor;
import com.sanedge.paymentgateway.pb.user.UserQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel userChannel(
            @Value("${grpc.client.user.host:localhost}") String host,
            @Value("${grpc.client.user.port:9011}") int port,
            DependencyGuardInterceptor dependencyGuardInterceptor) {
        return NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean
    public UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryServiceBlockingStub(ManagedChannel userChannel) {
        return UserQueryServiceGrpc.newBlockingStub(userChannel);
    }

    @Bean
    public UserPort userPort(UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryServiceBlockingStub) {
        return new UserAdapter(userQueryServiceBlockingStub);
    }
}
