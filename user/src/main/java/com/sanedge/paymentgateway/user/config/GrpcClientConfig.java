package com.sanedge.paymentgateway.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanedge.paymentgateway.common.adapter.role.RoleAdapter;
import com.sanedge.paymentgateway.common.adapter.role.RolePort;
import com.sanedge.paymentgateway.common.adapter.role.UserRoleAdapter;
import com.sanedge.paymentgateway.common.adapter.role.UserRolePort;
import com.sanedge.paymentgateway.common.resilience.DependencyGuardInterceptor;
import com.sanedge.paymentgateway.pb.role.RoleCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.role.RoleServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel roleChannel(
            @Value("${grpc.client.role.host:localhost}") String host,
            @Value("${grpc.client.role.port:9006}") int port,
            DependencyGuardInterceptor dependencyGuardInterceptor) {
        return NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .intercept(dependencyGuardInterceptor)
                .build();
    }

    @Bean
    public RoleServiceGrpc.RoleServiceBlockingStub roleServiceBlockingStub(ManagedChannel roleChannel) {
        return RoleServiceGrpc.newBlockingStub(roleChannel);
    }

    @Bean
    public RoleCommandServiceGrpc.RoleCommandServiceBlockingStub roleCommandServiceBlockingStub(
            ManagedChannel roleChannel) {
        return RoleCommandServiceGrpc.newBlockingStub(roleChannel);
    }

    @Bean
    public RolePort rolePort(RoleServiceGrpc.RoleServiceBlockingStub roleServiceBlockingStub) {
        return new RoleAdapter(roleServiceBlockingStub);
    }

    @Bean
    public UserRolePort userRolePort(RoleServiceGrpc.RoleServiceBlockingStub roleServiceBlockingStub,
            RoleCommandServiceGrpc.RoleCommandServiceBlockingStub roleCommandServiceBlockingStub) {
        return new UserRoleAdapter(roleServiceBlockingStub, roleCommandServiceBlockingStub);
    }
}
