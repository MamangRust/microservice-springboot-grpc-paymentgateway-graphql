package com.sanedge.paymentgateway.card.repository.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.card.models.Card;

@Repository
public interface CardDashboardRepository extends JpaRepository<Card, Long> {
    @Query(value = """
            SELECT COALESCE(SUM(s.total_balance), 0)
            FROM saldos s
            JOIN cards c ON s.card_number = c.card_number
            WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL
            """, nativeQuery = true)
    Long getTotalBalance();

    @Query(value = """
            SELECT COALESCE(SUM(t.topup_amount), 0)
            FROM topups t
            JOIN cards c ON t.card_number = c.card_number
            WHERE t.deleted_at IS NULL AND c.deleted_at IS NULL
            """, nativeQuery = true)
    Long getTotalTopupAmount();

    @Query(value = """
            SELECT COALESCE(SUM(s.withdraw_amount), 0)
            FROM withdraws s
            JOIN cards c ON s.card_number = c.card_number
            WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL
            """, nativeQuery = true)
    Long getTotalWithdrawAmount();

    @Query(value = """
            SELECT COALESCE(SUM(t.amount), 0)
            FROM transactions t
            JOIN cards c ON t.card_number = c.card_number
            WHERE t.deleted_at IS NULL AND c.deleted_at IS NULL
            """, nativeQuery = true)
    Long getTotalTransactionAmount();

    @Query(value = """
            SELECT COALESCE(SUM(transfer_amount), 0)
            FROM (
                SELECT transfer_amount FROM transfers WHERE deleted_at IS NULL
                UNION ALL
                SELECT transfer_amount FROM transfers WHERE deleted_at IS NULL
            ) AS transfer_data
            """, nativeQuery = true)
    Long getTotalTransferAmount();
}
