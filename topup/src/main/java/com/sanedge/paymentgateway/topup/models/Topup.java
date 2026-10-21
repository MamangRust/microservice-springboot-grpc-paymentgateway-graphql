package com.sanedge.paymentgateway.topup.models;

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
@Table(name = "topups")
public class Topup extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topup_id")
    private Long topupId;

    @Column(name = "topup_no", nullable = false, unique = true)
    private UUID topupNo;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "topup_amount", nullable = false)
    private Long topupAmount;

    @Column(name = "topup_method", nullable = false)
    private String topupMethod;

    @Column(name = "topup_time", nullable = false)
    private Timestamp topupTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;
}
