package com.sanedge.paymentgateway.common.adapter.user;

import com.sanedge.paymentgateway.common.adapter.model.User;

/**
 * Minimal cross-service user surface other domains need (e.g. merchant and card
 * resolving the owner of a record), replacing direct {@code @GrpcClient("user")}
 * usage.
 */
public interface UserPort {

    User findById(int userId);
}
