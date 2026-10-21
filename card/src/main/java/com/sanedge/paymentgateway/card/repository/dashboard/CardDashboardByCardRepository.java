package com.sanedge.paymentgateway.card.repository.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.card.models.Card;

@Repository
public interface CardDashboardByCardRepository extends JpaRepository<Card, Long> {
  @Query(value = """
      SELECT COALESCE(SUM(s.total_balance), 0)
      FROM saldos s
      JOIN cards c ON s.card_number = c.card_number
      WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL
        AND c.card_number = :cardNumber
      """, nativeQuery = true)
  Long getTotalBalanceByCard(@Param("cardNumber") String cardNumber);

  @Query(value = """
      SELECT COALESCE(SUM(t.topup_amount), 0)
      FROM topups t
      JOIN cards c ON t.card_number = c.card_number
      WHERE t.deleted_at IS NULL AND c.deleted_at IS NULL
        AND c.card_number = :cardNumber
      """, nativeQuery = true)
  Long getTotalTopupAmountByCard(@Param("cardNumber") String cardNumber);

  @Query(value = """
      SELECT COALESCE(SUM(s.withdraw_amount), 0)
      FROM withdraws s
      JOIN cards c ON s.card_number = c.card_number
      WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL
        AND c.card_number = :cardNumber
      """, nativeQuery = true)
  Long getTotalWithdrawAmountByCard(@Param("cardNumber") String cardNumber);

  @Query(value = """
      SELECT COALESCE(SUM(t.amount), 0)
      FROM transactions t
      JOIN cards c ON t.card_number = c.card_number
      WHERE t.deleted_at IS NULL AND c.deleted_at IS NULL
        AND c.card_number = :cardNumber
      """, nativeQuery = true)
  Long getTotalTransactionAmountByCard(@Param("cardNumber") String cardNumber);

  @Query(value = """
      SELECT COALESCE(SUM(transfer_amount), 0)
      FROM transfers
      WHERE transfer_from = :sender
        AND deleted_at IS NULL
      """, nativeQuery = true)
  Long getTotalTransferAmountBySender(@Param("sender") String sender);

  @Query(value = """
      SELECT COALESCE(SUM(transfer_amount), 0)
      FROM transfers
      WHERE transfer_to = :receiver
        AND deleted_at IS NULL
      """, nativeQuery = true)
  Long getTotalTransferAmountByReceiver(@Param("receiver") String receiver);
}
