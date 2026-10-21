package com.sanedge.paymentgateway.saldo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.saldo.models.Saldo;

@Repository
public interface SaldoQueryRepository extends JpaRepository<Saldo, Long> {
    @Query("""
                SELECT s FROM Saldo s
                WHERE s.deletedAt IS NULL
                AND (:keyword IS NULL OR LOWER(s.cardNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY s.saldoId ASC
            """)
    Page<Saldo> findSaldos(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
            SELECT saldo_id, card_number, total_balance, withdraw_amount, withdraw_time,
                   created_at, updated_at, deleted_at
            FROM saldos
            WHERE card_number = :cardNumber
              AND deleted_at IS NULL
            LIMIT 1
            """, nativeQuery = true)
    Optional<Saldo> findByCardNumber(@Param("cardNumber") String cardNumber);

    @Query("""
                SELECT s FROM Saldo s
                WHERE s.deletedAt IS NULL
                AND (:keyword IS NULL OR LOWER(s.cardNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY s.saldoId ASC
            """)
    Page<Saldo> findActiveSaldos(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT s FROM Saldo s
                WHERE s.deletedAt IS NOT NULL
                AND (:keyword IS NULL OR LOWER(s.cardNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY s.saldoId ASC
            """)
    Page<Saldo> findTrashedSaldos(@Param("keyword") String keyword, Pageable pageable);
}
