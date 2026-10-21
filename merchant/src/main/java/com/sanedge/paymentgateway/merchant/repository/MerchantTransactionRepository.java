package com.sanedge.paymentgateway.merchant.repository;

import com.sanedge.paymentgateway.merchant.models.MerchantTransactions;
import com.sanedge.paymentgateway.merchant.models.Transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantTransactionRepository extends JpaRepository<Transaction, Long> {
    @Query(value = """
            SELECT
                t.transaction_id AS transactionId,
                t.card_number AS cardNumber,
                t.amount,
                t.payment_method AS paymentMethod,
                t.merchant_id AS merchantId,
                m.name AS merchantName,
                t.transaction_time AS transactionTime,
                t.created_at AS createdAt,
                t.updated_at AS updatedAt,
                t.deleted_at AS deletedAt
            FROM transactions t
            JOIN merchants m ON t.merchant_id = m.merchant_id
            WHERE t.deleted_at IS NULL
              AND (:search IS NULL OR t.card_number ILIKE '%' || :search || '%' OR t.payment_method ILIKE '%' || :search || '%')
            ORDER BY t.transaction_time DESC
            """, nativeQuery = true)
    Page<MerchantTransactions> findAllTransactions(@Param("search") String search, Pageable pageable);

    @Query(value = """
            SELECT
                t.transaction_id AS transactionId,
                t.card_number AS cardNumber,
                t.amount,
                t.payment_method AS paymentMethod,
                t.merchant_id AS merchantId,
                m.name AS merchantName,
                t.transaction_time AS transactionTime,
                t.created_at AS createdAt,
                t.updated_at AS updatedAt,
                t.deleted_at AS deletedAt
            FROM transactions t
            JOIN merchants m ON t.merchant_id = m.merchant_id
            WHERE t.deleted_at IS NULL
              AND t.merchant_id = :merchantId
              AND (:search IS NULL OR t.card_number ILIKE '%' || :search || '%' OR t.payment_method ILIKE '%' || :search || '%')
            ORDER BY t.transaction_time DESC
            """, nativeQuery = true)
    Page<MerchantTransactions> findAllTransactionsByMerchant(
            @Param("merchantId") Long merchantId,
            @Param("search") String search,
            Pageable pageable);

    @Query(value = """
            SELECT
                t.transaction_id AS transactionId,
                t.card_number AS cardNumber,
                t.amount,
                t.payment_method AS paymentMethod,
                t.merchant_id AS merchantId,
                m.name AS merchantName,
                t.transaction_time AS transactionTime,
                t.created_at AS createdAt,
                t.updated_at AS updatedAt,
                t.deleted_at AS deletedAt
            FROM transactions t
            JOIN merchants m ON t.merchant_id = m.merchant_id
            WHERE t.deleted_at IS NULL
              AND m.api_key = :apiKey
              AND (:search IS NULL OR t.card_number ILIKE '%' || :search || '%' OR t.payment_method ILIKE '%' || :search || '%')
            ORDER BY t.transaction_time DESC
            """, nativeQuery = true)
    Page<MerchantTransactions> findAllTransactionsByApiKey(
            @Param("apiKey") String apiKey,
            @Param("search") String search,
            Pageable pageable);
}
