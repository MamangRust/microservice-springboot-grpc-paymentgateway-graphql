package com.sanedge.paymentgateway.card.repository;

import com.sanedge.paymentgateway.card.models.Card;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardQueryRepository extends JpaRepository<Card, Long> {
    @Query("""
            SELECT c FROM Card c
            WHERE c.deletedAt IS NULL
            AND (
                :keyword IS NULL OR
                LOWER(c.cardNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(c.cardType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(c.cardProvider) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Card> findCards(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT c FROM Card c
            WHERE c.deletedAt IS NULL
            AND (
                :keyword IS NULL OR
                LOWER(c.cardNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(c.cardType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(c.cardProvider) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Card> findActiveCards(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT c FROM Card c
            WHERE c.deletedAt IS NOT NULL
            AND (
                :keyword IS NULL OR
                LOWER(c.cardNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(c.cardType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(c.cardProvider) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Card> findTrashedCards(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM cards WHERE card_id = :cardId AND deleted_at IS NULL LIMIT 1", nativeQuery = true)
    Optional<Card> findCardById(@Param("cardId") Long cardId);

    @Query(value = "SELECT * FROM cards WHERE user_id = :userId AND deleted_at IS NULL LIMIT 1", nativeQuery = true)
    Optional<Card> findCardByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM cards WHERE card_number = :cardNumber AND deleted_at IS NULL", nativeQuery = true)
    Optional<Card> findCardByCardNumber(@Param("cardNumber") String cardNumber);
}
