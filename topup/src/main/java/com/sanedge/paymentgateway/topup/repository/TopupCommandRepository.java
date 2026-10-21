package com.sanedge.paymentgateway.topup.repository;

import com.sanedge.paymentgateway.topup.models.Topup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopupCommandRepository extends JpaRepository<Topup, Long>, TopupCommandRepositoryCustom {

}
