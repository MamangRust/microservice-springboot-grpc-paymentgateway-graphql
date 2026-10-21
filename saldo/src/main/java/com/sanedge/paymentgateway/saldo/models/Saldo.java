package com.sanedge.paymentgateway.saldo.models;

import java.sql.Timestamp;

import com.sanedge.paymentgateway.common.models.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "saldos")
public class Saldo extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saldo_id")
    private Long saldoId;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "total_balance", nullable = false)
    private Long totalBalance;

    @Column(name = "withdraw_amount", nullable = true)
    private Long withdrawAmount;

    @Column(name = "withdraw_time", nullable = true)
    private Timestamp withdrawTime;

}
