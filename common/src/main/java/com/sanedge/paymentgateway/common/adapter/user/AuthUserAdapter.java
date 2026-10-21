package com.sanedge.paymentgateway.common.adapter.user;

import java.util.Optional;

import com.sanedge.paymentgateway.common.adapter.model.User;
import com.sanedge.paymentgateway.common.adapter.support.AdapterException;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.pb.user.CreateUserRequest;
import com.sanedge.paymentgateway.pb.user.FindAllUserRequest;
import com.sanedge.paymentgateway.pb.user.FindByIdUserRequest;
import com.sanedge.paymentgateway.pb.user.UpdateUserRequest;
import com.sanedge.paymentgateway.pb.user.UserCommandServiceGrpc.UserCommandServiceBlockingStub;
import com.sanedge.paymentgateway.pb.user.UserQueryServiceGrpc.UserQueryServiceBlockingStub;
import com.sanedge.paymentgateway.pb.user.UserResponse;
import com.sanedge.paymentgateway.pb.user.VerifyPasswordRequest;

public class AuthUserAdapter implements AuthUserPort {

    private final UserQueryServiceBlockingStub query;

    private final UserCommandServiceBlockingStub command;

    public AuthUserAdapter(UserQueryServiceBlockingStub query, UserCommandServiceBlockingStub command) {
        this.query = query;
        this.command = command;
    }

    @Override
    public User findById(int userId) {
        var resp = query.findById(FindByIdUserRequest.newBuilder().setId(userId).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return UserAdapter.toUser(resp.getData());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        var resp = query.findAll(FindAllUserRequest.newBuilder()
                .setSearch(email)
                .setPage(1)
                .setPageSize(1)
                .build());
        if (resp == null) {
            return Optional.empty();
        }
        for (UserResponse u : resp.getDataList()) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return Optional.of(UserAdapter.toUser(u));
            }
        }
        return Optional.empty();
    }

    @Override
    public User create(RegisterData data) {
        var resp = command.create(CreateUserRequest.newBuilder()
                .setFirstname(nullSafe(data.firstname()))
                .setLastname(nullSafe(data.lastname()))
                .setEmail(nullSafe(data.email()))
                .setPassword(nullSafe(data.password()))
                .setConfirmPassword(nullSafe(data.confirmPassword()))
                .build());
        if (resp == null || !"success".equalsIgnoreCase(resp.getStatus())) {
            throw new AdapterException(resp == null ? "create user failed"
                    : "create user failed: " + resp.getMessage());
        }
        if (!resp.hasData()) {
            throw new AdapterException("create user returned no data");
        }
        return UserAdapter.toUser(resp.getData());
    }

    @Override
    public VerifyResult verifyPassword(String email, String password) {
        var resp = command.verifyPassword(VerifyPasswordRequest.newBuilder()
                .setEmail(nullSafe(email))
                .setPassword(nullSafe(password))
                .build());
        if (resp == null) {
            return new VerifyResult(false, null);
        }
        User user = resp.hasUser() ? UserAdapter.toUser(resp.getUser()) : null;
        return new VerifyResult(resp.getValid(), user);
    }

    @Override
    public User update(UpdateData data) {
        var resp = command.update(UpdateUserRequest.newBuilder()
                .setId(data.id())
                .setFirstname(nullSafe(data.firstname()))
                .setLastname(nullSafe(data.lastname()))
                .setEmail(nullSafe(data.email()))
                .setPassword(nullSafe(data.password()))
                .setConfirmPassword(nullSafe(data.confirmPassword()))
                .build());
        if (resp == null || !"success".equalsIgnoreCase(resp.getStatus())) {
            throw new AdapterException(resp == null ? "update user failed"
                    : "update user failed: " + resp.getMessage());
        }
        if (!resp.hasData()) {
            throw new AdapterException("update user returned no data");
        }
        return UserAdapter.toUser(resp.getData());
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
