package com.sanedge.paymentgateway.withdraw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.withdraw.models.Withdraw;

@Repository
public interface WithdrawCommandRepository extends JpaRepository<Withdraw, Long>, WithdrawCommandRepositoryCustom {
}
