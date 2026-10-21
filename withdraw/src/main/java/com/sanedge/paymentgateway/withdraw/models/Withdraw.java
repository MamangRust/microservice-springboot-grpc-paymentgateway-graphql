package com.sanedge.paymentgateway.withdraw.models;

import java.sql.Timestamp;
import java.util.UUID;

import com.sanedge.paymentgateway.common.enums.Status;
import com.sanedge.paymentgateway.common.models.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "withdraws")
public class Withdraw extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdraw_id")
    private Long withdrawId;

    @Column(name = "withdraw_no", nullable = false, unique = true)
    private UUID withdrawNo;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "withdraw_amount", nullable = false)
    private Long withdrawAmount;

    @Column(name = "withdraw_time", nullable = false)
    private Timestamp withdrawTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;
}
