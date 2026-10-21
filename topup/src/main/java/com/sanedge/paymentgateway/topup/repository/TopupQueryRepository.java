package com.sanedge.paymentgateway.topup.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.topup.models.Topup;

@Repository
public interface TopupQueryRepository extends JpaRepository<Topup, Long> {
    @Query("""
                SELECT t FROM Topup t
                WHERE t.deletedAt IS NULL
                  AND (
                    :search IS NULL
                    OR LOWER(t.cardNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(t.topupNo AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(t.topupMethod) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
                ORDER BY t.topupTime DESC
            """)
    Page<Topup> findTopups(@Param("search") String search, Pageable pageable);

    @Query("""
                SELECT t FROM Topup t
                WHERE t.deletedAt IS NULL
                  AND t.cardNumber = :cardNumber
                  AND (
                    :search IS NULL
                    OR LOWER(CAST(t.topupNo AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(t.topupMethod) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
                ORDER BY t.topupTime DESC
            """)
    Page<Topup> findTopupByCard(
            @Param("cardNumber") String cardNumber,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
                SELECT t FROM Topup t
                WHERE t.deletedAt IS NULL
                  AND (
                    :search IS NULL
                    OR LOWER(t.cardNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(t.topupNo AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(t.topupMethod) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
                ORDER BY t.topupTime DESC
            """)
    Page<Topup> findActiveTopups(@Param("search") String search, Pageable pageable);

    @Query("""
                SELECT t FROM Topup t
                WHERE t.deletedAt IS NOT NULL
                  AND (
                    :search IS NULL
                    OR LOWER(t.cardNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(t.topupNo AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(t.topupMethod) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
                ORDER BY t.topupTime DESC
            """)
    Page<Topup> findTrashedTopups(@Param("search") String search, Pageable pageable);

    @Query("""
                SELECT t FROM Topup t
                WHERE t.cardNumber = :cardNumber
                  AND t.deletedAt IS NULL
                ORDER BY t.topupTime DESC
            """)
    List<Topup> findByCardNumber(@Param("cardNumber") String cardNumber);

    @Query("""
                SELECT t FROM Topup t
                WHERE t.topupId = :id AND t.deletedAt IS NULL
            """)
    Topup findTopupById(@Param("id") Long id);
}
