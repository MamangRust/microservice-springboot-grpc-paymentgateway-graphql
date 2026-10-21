package com.sanedge.paymentgateway.user.repository;

import com.sanedge.paymentgateway.user.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCommandRepository extends JpaRepository<User, Long>, UserCommandRepositoryCustom {

}
