package com.sanedge.paymentgateway.saldo.repository;

import com.sanedge.paymentgateway.saldo.models.Saldo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaldoCommandRepository extends JpaRepository<Saldo, Long>, SaldoCommandRepositoryCustom {

}
