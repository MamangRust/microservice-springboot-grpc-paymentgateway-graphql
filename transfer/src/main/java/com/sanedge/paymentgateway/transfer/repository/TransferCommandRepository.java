package com.sanedge.paymentgateway.transfer.repository;

import com.sanedge.paymentgateway.transfer.models.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferCommandRepository extends JpaRepository<Transfer, Long>, TransferCommandRepositoryCustom {

}
