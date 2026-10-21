package com.sanedge.paymentgateway.card.repository;

import com.sanedge.paymentgateway.card.models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardCommandRepository extends JpaRepository<Card, Long>, CardCommandRepositoryCustom {

}
