package com.sanedge.paymentgateway.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.paymentgateway.common.adapter.role.RoleAdapter;
import com.sanedge.paymentgateway.common.adapter.role.RolePort;
import com.sanedge.paymentgateway.common.adapter.user.AuthUserAdapter;
import com.sanedge.paymentgateway.common.adapter.user.AuthUserPort;
import com.sanedge.paymentgateway.common.resilience.DependencyGuardInterceptor;
import com.sanedge.paymentgateway.pb.role.RoleServiceGrpc;
import com.sanedge.paymentgateway.pb.user.UserCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.user.UserQueryServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

/** Outbound gRPC channels/stubs used by the auth service (user + role). */
@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.user.host:localhost}")
    private String userHost;

    @Value("${grpc.client.user.port:9011}")
    private int userPort;

    @Value("${grpc.client.role.host:localhost}")
    private String roleHost;

    @Value("${grpc.client.role.port:9006}")
    private int rolePort;

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel userChannel(DependencyGuardInterceptor dependencyGuardInterceptor) {
        return NettyChannelBuilder.forAddress(userHost, userPort)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel roleChannel(DependencyGuardInterceptor dependencyGuardInterceptor) {
        return NettyChannelBuilder.forAddress(roleHost, rolePort)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean
    public UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub(
            @Qualifier("userChannel") ManagedChannel userChannel) {
        return UserQueryServiceGrpc.newBlockingStub(userChannel);
    }

    @Bean
    public UserCommandServiceGrpc.UserCommandServiceBlockingStub userCommandStub(
            @Qualifier("userChannel") ManagedChannel userChannel) {
        return UserCommandServiceGrpc.newBlockingStub(userChannel);
    }

    @Bean
    public RoleServiceGrpc.RoleServiceBlockingStub roleQueryStub(
            @Qualifier("roleChannel") ManagedChannel roleChannel) {
        return RoleServiceGrpc.newBlockingStub(roleChannel);
    }

    @Bean
    public AuthUserPort authUserPort(UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub,
            UserCommandServiceGrpc.UserCommandServiceBlockingStub userCommandStub) {
        return new AuthUserAdapter(userQueryStub, userCommandStub);
    }

    @Bean
    public RolePort rolePort(RoleServiceGrpc.RoleServiceBlockingStub roleQueryStub) {
        return new RoleAdapter(roleQueryStub);
    }
}
