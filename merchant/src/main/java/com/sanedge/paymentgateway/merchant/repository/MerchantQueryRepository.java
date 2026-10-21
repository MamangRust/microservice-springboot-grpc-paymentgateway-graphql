package com.sanedge.paymentgateway.merchant.repository;

import com.sanedge.paymentgateway.merchant.models.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MerchantQueryRepository extends JpaRepository<Merchant, Long> {

    @Query("""
            SELECT COUNT(m) > 0 FROM Merchant m
            WHERE LOWER(m.name) = LOWER(:name)
              AND m.deletedAt IS NULL
            """)
    boolean existsByName(@Param("name") String name);

    @Query("""
                SELECT m FROM Merchant m
                WHERE (
                    :keyword IS NULL
                    OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(m.apiKey) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(CAST(m.status AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                ORDER BY m.merchantId ASC
            """)
    Page<Merchant> findMerchants(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT m FROM Merchant m
                WHERE m.deletedAt IS NULL
                  AND (
                    :keyword IS NULL
                    OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(m.apiKey) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(CAST(m.status AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
                ORDER BY m.merchantId ASC
            """)
    Page<Merchant> findActiveMerchants(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT m FROM Merchant m
                WHERE m.deletedAt IS NOT NULL
                  AND (
                    :keyword IS NULL
                    OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(m.apiKey) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(CAST(m.status AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
                ORDER BY m.merchantId DESC
            """)
    Page<Merchant> findTrashedMerchants(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM merchants WHERE merchant_id = :merchantId AND deleted_at IS NULL", nativeQuery = true)
    Optional<Merchant> findMerchantById(@Param("merchantId") Long merchantId);

    @Query(value = "SELECT * FROM merchants WHERE api_key = :apiKey AND deleted_at IS NULL", nativeQuery = true)
    Optional<Merchant> findByApiKey(@Param("apiKey") String apiKey);

    @Query(value = "SELECT * FROM merchants WHERE name = :name AND deleted_at IS NULL", nativeQuery = true)
    Optional<Merchant> findByName(@Param("name") String name);

    @Query(value = "SELECT * FROM merchants WHERE user_id = :userId AND deleted_at IS NULL", nativeQuery = true)
    List<Merchant> findByUserId(@Param("userId") Long userId);

}
