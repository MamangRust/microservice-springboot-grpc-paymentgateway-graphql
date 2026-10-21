package com.sanedge.paymentgateway.transfer.models;

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
@Table(name = "transfers")
public class Transfer extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;

    @Column(name = "transfer_no", nullable = false, unique = true)
    private UUID transferNo;

    @Column(name = "transfer_from", nullable = false)
    private String transferFrom;

    @Column(name = "transfer_to", nullable = false)
    private String transferTo;

    @Column(name = "transfer_amount", nullable = false)
    private Long transferAmount;

    @Column(name = "transfer_time", nullable = false)
    private Timestamp transferTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;
}
