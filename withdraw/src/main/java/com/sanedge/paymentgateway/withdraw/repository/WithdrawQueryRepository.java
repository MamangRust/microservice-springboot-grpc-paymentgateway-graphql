package com.sanedge.paymentgateway.withdraw.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.withdraw.models.Withdraw;

@Repository
public interface WithdrawQueryRepository extends JpaRepository<Withdraw, Long> {

    @Query("""
                SELECT w
                FROM Withdraw w
                WHERE w.deletedAt IS NULL
                  AND (
                      :keyword IS NULL
                      OR w.cardNumber LIKE %:keyword%
                      OR CAST(w.withdrawAmount AS string) LIKE %:keyword%
                      OR CAST(w.withdrawTime AS string) LIKE %:keyword%
                      OR LOWER(CAST(w.status AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
                ORDER BY w.withdrawTime DESC
            """)
    Page<Withdraw> findAllWithdraws(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT w
                FROM Withdraw w
                WHERE w.deletedAt IS NULL
                  AND (
                      :keyword IS NULL
                      OR w.cardNumber LIKE %:keyword%
                      OR CAST(w.withdrawAmount AS string) LIKE %:keyword%
                      OR CAST(w.withdrawTime AS string) LIKE %:keyword%
                      OR LOWER(CAST(w.status AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
                ORDER BY w.withdrawTime DESC
            """)
    Page<Withdraw> findActiveWithdraws(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT w
                FROM Withdraw w
                WHERE w.deletedAt IS NOT NULL
                  AND (
                      :keyword IS NULL
                      OR w.cardNumber LIKE %:keyword%
                      OR CAST(w.withdrawAmount AS string) LIKE %:keyword%
                      OR CAST(w.withdrawTime AS string) LIKE %:keyword%
                      OR LOWER(CAST(w.status AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
                ORDER BY w.withdrawTime DESC
            """)
    Page<Withdraw> findTrashedWithdraws(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
            SELECT
                withdraw_id,
                withdraw_no,
                card_number,
                withdraw_amount,
                withdraw_time,
                status,
                created_at,
                updated_at,
                deleted_at
            FROM
                withdraws
            WHERE
                card_number = :cardNumber
                AND deleted_at IS NULL
            ORDER BY withdraw_time DESC
            """, nativeQuery = true)
    List<Withdraw> findByCardNumber(@Param("cardNumber") String cardNumber);

    @Query("""
                SELECT w
                FROM Withdraw w
                WHERE w.deletedAt IS NULL
                AND w.cardNumber = :cardNumber
                AND (
                    :keyword IS NULL
                    OR CAST(w.withdrawAmount AS string) LIKE CONCAT('%', :keyword, '%')
                    OR CAST(w.withdrawTime AS string) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(CAST(w.status AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                ORDER BY w.withdrawTime DESC
            """)
    Page<Withdraw> findAllByCardNumber(
            @Param("cardNumber") String cardNumber,
            @Param("keyword") String keyword,
            Pageable pageable);

}
