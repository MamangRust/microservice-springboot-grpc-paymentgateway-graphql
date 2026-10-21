package com.sanedge.paymentgateway.transaction.repository;

import com.sanedge.paymentgateway.transaction.models.Transaction;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionQueryRepository extends JpaRepository<Transaction, Long> {

  @Query("""
          SELECT t FROM Transaction t
          WHERE t.deletedAt IS NULL
            AND (
              :search IS NULL
              OR LOWER(t.cardNumber) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(t.paymentMethod) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            )
          ORDER BY t.transactionTime DESC
      """)
  Page<Transaction> findTransactions(@Param("search") String search, Pageable pageable);

  @Query("""
          SELECT t FROM Transaction t
          WHERE t.deletedAt IS NULL
            AND (
              :search IS NULL
              OR LOWER(t.cardNumber) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(t.paymentMethod) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            )
          ORDER BY t.transactionTime DESC
      """)
  Page<Transaction> findActiveTransactions(@Param("search") String search, Pageable pageable);

  @Query("""
          SELECT t FROM Transaction t
          WHERE t.deletedAt IS NOT NULL
            AND (
              :search IS NULL
              OR LOWER(t.cardNumber) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(t.paymentMethod) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            )
          ORDER BY t.transactionTime DESC
      """)
  Page<Transaction> findTrashedTransactions(@Param("search") String search, Pageable pageable);

  @Query("SELECT t FROM Transaction t WHERE t.transactionId = :id AND t.deletedAt IS NULL")
  Transaction findTransactionById(@Param("id") Long id);

  @Query("""
          SELECT t FROM Transaction t
          WHERE t.deletedAt IS NULL
            AND t.cardNumber = :cardNumber
            AND (
              :filter IS NULL
              OR LOWER(t.paymentMethod) LIKE LOWER(CONCAT('%', :filter, '%'))
              OR LOWER(t.status) LIKE LOWER(CONCAT('%', :filter, '%'))
            )
          ORDER BY t.transactionTime DESC
      """)
  Page<Transaction> findTransactionsByCardNumber(@Param("cardNumber") String cardNumber,
      @Param("filter") String filter,
      Pageable pageable);

  @Query("SELECT t FROM Transaction t WHERE t.merchantId = :merchantId AND t.deletedAt IS NULL ORDER BY t.transactionTime DESC")
  List<Transaction> findTransactionsByMerchantId(@Param("merchantId") Long merchantId);

}
