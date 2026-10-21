package com.sanedge.paymentgateway.transfer.repository;

import com.sanedge.paymentgateway.transfer.models.Transfer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferQueryRepository extends JpaRepository<Transfer, Long> {
  @Query("""
          SELECT t FROM Transfer t
          WHERE t.deletedAt IS NULL
            AND (
                :search IS NULL
                OR LOWER(t.transferFrom) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(t.transferTo) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            )
          ORDER BY t.transferTime DESC
      """)
  Page<Transfer> findTransfers(@Param("search") String search, Pageable pageable);

  @Query("""
          SELECT t FROM Transfer t
          WHERE t.deletedAt IS NULL
            AND (
                :search IS NULL
                OR LOWER(t.transferFrom) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(t.transferTo) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            )
          ORDER BY t.transferTime DESC
      """)
  Page<Transfer> findActiveTransfers(@Param("search") String search, Pageable pageable);

  @Query("""
          SELECT t FROM Transfer t
          WHERE t.deletedAt IS NOT NULL
            AND (
                :search IS NULL
                OR LOWER(t.transferFrom) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(t.transferTo) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            )
          ORDER BY t.transferTime DESC
      """)
  Page<Transfer> findTrashedTransfers(@Param("search") String search, Pageable pageable);

  @Query(value = """
      SELECT *
      FROM transfers
      WHERE transfer_id = :id
        AND deleted_at IS NULL
      """, nativeQuery = true)
  Optional<Transfer> findTransferById(@Param("id") Long id);

  @Query(value = """
      SELECT *
      FROM transfers
      WHERE deleted_at IS NULL
        AND (transfer_from = :cardNumber OR transfer_to = :cardNumber)
      ORDER BY transfer_time DESC
      """, nativeQuery = true)
  List<Transfer> findTransfersByCardNumber(@Param("cardNumber") String cardNumber);

  @Query(value = """
      SELECT *
      FROM transfers
      WHERE deleted_at IS NULL
        AND transfer_from = :cardNumber
      ORDER BY transfer_time DESC
      """, nativeQuery = true)
  List<Transfer> findTransfersBySourceCard(@Param("cardNumber") String cardNumber);

  @Query(value = """
      SELECT *
      FROM transfers
      WHERE deleted_at IS NULL
        AND transfer_to = :cardNumber
      ORDER BY transfer_time DESC
      """, nativeQuery = true)
  List<Transfer> findTransfersByDestinationCard(@Param("cardNumber") String cardNumber);
}
