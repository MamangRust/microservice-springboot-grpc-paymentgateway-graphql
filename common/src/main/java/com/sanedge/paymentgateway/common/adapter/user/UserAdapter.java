package com.sanedge.paymentgateway.common.adapter.user;

import com.sanedge.paymentgateway.common.adapter.model.User;
import com.sanedge.paymentgateway.common.adapter.support.ProtoTime;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.pb.user.FindByIdUserRequest;
import com.sanedge.paymentgateway.pb.user.UserQueryServiceGrpc.UserQueryServiceBlockingStub;
import com.sanedge.paymentgateway.pb.user.UserResponse;

public class UserAdapter implements UserPort {

    private final UserQueryServiceBlockingStub query;

    public UserAdapter(UserQueryServiceBlockingStub query) {
        this.query = query;
    }

    @Override
    public User findById(int userId) {
        var resp = query.findById(FindByIdUserRequest.newBuilder().setId(userId).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return toUser(resp.getData());
    }

    static User toUser(UserResponse u) {
        if (u == null) {
            return null;
        }
        return new User(u.getId(), u.getFirstname(), u.getLastname(), u.getEmail(),
                ProtoTime.parse(u.getCreatedAt()), ProtoTime.parse(u.getUpdatedAt()));
    }
}
