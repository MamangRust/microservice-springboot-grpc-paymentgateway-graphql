package com.sanedge.paymentgateway.gateway.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.google.protobuf.Timestamp;
import com.sanedge.paymentgateway.gateway.dto.responses.auth.TokenResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.card.CardResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.card.CardResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.card.dashboard.CardDashboard;
import com.sanedge.paymentgateway.gateway.dto.responses.card.dashboard.CardDashboardCard;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.MerchantResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.MerchantResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.merchant.MerchantTransactionResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.role.RoleResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.role.RoleResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.saldo.SaldoResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.saldo.SaldoResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.stats.StatItemResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.TopupResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.topup.TopupResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.TransactionResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transaction.TransactionResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.TransferResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.transfer.TransferResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.user.UserResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.user.UserResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.WithdrawResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.withdraw.WithdrawResponseDeleteAt;

/**
 * Maps generated protobuf response messages onto the gateway-local REST DTOs so
 * that the JSON contract stays identical to the former monolith. Protobuf types
 * are fully qualified to avoid simple-name collisions with the gateway DTOs.
 */
public final class ProtoMapper {

    private ProtoMapper() {
    }

    private static String str(com.google.protobuf.StringValue value) {
        return value == null ? null : value.getValue();
    }

    // ---- timestamps (REST DTO -> proto) ----
    public static Timestamp toTimestamp(LocalDateTime value) {
        if (value == null) {
            return Timestamp.getDefaultInstance();
        }
        return Timestamp.newBuilder()
                .setSeconds(value.toEpochSecond(ZoneOffset.UTC))
                .setNanos(value.getNano())
                .build();
    }

    public static Timestamp toTimestamp(LocalDate value) {
        if (value == null) {
            return Timestamp.getDefaultInstance();
        }
        return toTimestamp(value.atStartOfDay());
    }

    // ---- stats (ClickHouse analytics reader) ----
    public static StatItemResponse statItem(com.sanedge.paymentgateway.pb.stats.StatItem p) {
        if (p == null) {
            return null;
        }
        return StatItemResponse.builder()
                .period(p.getPeriod())
                .dimensionId(p.getDimensionId())
                .value(p.getValue())
                .build();
    }

    public static List<StatItemResponse> statItems(List<com.sanedge.paymentgateway.pb.stats.StatItem> list) {
        return list.stream().map(ProtoMapper::statItem).toList();
    }

    // ---- auth ----
    public static TokenResponse token(com.sanedge.paymentgateway.pb.TokenResponse p) {
        if (p == null) {
            return null;
        }
        return TokenResponse.builder()
                .access_token(p.getAccessToken())
                .refresh_token(p.getRefreshToken())
                .build();
    }

    // ---- user ----
    public static UserResponse user(com.sanedge.paymentgateway.pb.user.UserResponse p) {
        if (p == null) {
            return null;
        }
        return UserResponse.builder()
                .id((long) p.getId())
                .username(p.getEmail())
                .firstname(p.getFirstname())
                .lastname(p.getLastname())
                .email(p.getEmail())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static UserResponseDeleteAt userDeleteAt(com.sanedge.paymentgateway.pb.user.UserResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return UserResponseDeleteAt.builder()
                .id((long) p.getId())
                .firstname(p.getFirstname())
                .lastname(p.getLastname())
                .email(p.getEmail())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static List<UserResponse> users(List<com.sanedge.paymentgateway.pb.user.UserResponse> list) {
        return list.stream().map(ProtoMapper::user).toList();
    }

    public static List<UserResponseDeleteAt> usersDeleteAt(
            List<com.sanedge.paymentgateway.pb.user.UserResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::userDeleteAt).toList();
    }

    // ---- role ----
    public static RoleResponse role(com.sanedge.paymentgateway.pb.role.RoleResponse p) {
        if (p == null) {
            return null;
        }
        return RoleResponse.builder()
                .id((long) p.getId())
                .name(p.getName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static RoleResponseDeleteAt roleDeleteAt(com.sanedge.paymentgateway.pb.role.RoleResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return RoleResponseDeleteAt.builder()
                .id((long) p.getId())
                .name(p.getName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static List<RoleResponse> roles(List<com.sanedge.paymentgateway.pb.role.RoleResponse> list) {
        return list.stream().map(ProtoMapper::role).toList();
    }

    public static List<RoleResponseDeleteAt> rolesDeleteAt(
            List<com.sanedge.paymentgateway.pb.role.RoleResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::roleDeleteAt).toList();
    }

    // ---- card ----
    public static CardResponse card(com.sanedge.paymentgateway.pb.card.CardResponse p) {
        if (p == null) {
            return null;
        }
        return CardResponse.builder()
                .id((long) p.getId())
                .userId((long) p.getUserId())
                .cardNumber(p.getCardNumber())
                .cardType(p.getCardType())
                .expireDate(p.getExpireDate())
                .cvv(p.getCvv())
                .cardProvider(p.getCardProvider())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static CardResponseDeleteAt cardDeleteAt(com.sanedge.paymentgateway.pb.card.CardResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return CardResponseDeleteAt.builder()
                .id((long) p.getId())
                .userId((long) p.getUserId())
                .cardNumber(p.getCardNumber())
                .cardType(p.getCardType())
                .expireDate(p.getExpireDate())
                .cvv(p.getCvv())
                .cardProvider(p.getCardProvider())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static List<CardResponse> cards(List<com.sanedge.paymentgateway.pb.card.CardResponse> list) {
        return list.stream().map(ProtoMapper::card).toList();
    }

    public static List<CardResponseDeleteAt> cardsDeleteAt(
            List<com.sanedge.paymentgateway.pb.card.CardResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::cardDeleteAt).toList();
    }

    public static CardDashboard cardDashboard(com.sanedge.paymentgateway.pb.card.CardResponseDashboard p) {
        if (p == null) {
            return null;
        }
        return CardDashboard.builder()
                .totalBalance(p.getTotalBalance())
                .totalTopup(p.getTotalTopup())
                .totalWithdraw(p.getTotalWithdraw())
                .totalTransaction(p.getTotalTransaction())
                .totalTransfer(p.getTotalTransfer())
                .build();
    }

    public static CardDashboardCard cardDashboardCard(
            com.sanedge.paymentgateway.pb.card.CardResponseDashboardCardNumber p) {
        if (p == null) {
            return null;
        }
        return CardDashboardCard.builder()
                .totalBalance(p.getTotalBalance())
                .totalTopup(p.getTotalTopup())
                .totalWithdraw(p.getTotalWithdraw())
                .totalTransaction(p.getTotalTransaction())
                .totalTransferSend(p.getTotalTransferSend())
                .totalTransferReceiver(p.getTotalTransferReceiver())
                .build();
    }

    // ---- merchant ----
    public static MerchantResponse merchant(com.sanedge.paymentgateway.pb.merchant.MerchantResponse p) {
        if (p == null) {
            return null;
        }
        return MerchantResponse.builder()
                .id((long) p.getId())
                .name(p.getName())
                .userId((long) p.getUserId())
                .apiKey(p.getApiKey())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static MerchantResponseDeleteAt merchantDeleteAt(
            com.sanedge.paymentgateway.pb.merchant.MerchantResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return MerchantResponseDeleteAt.builder()
                .id((long) p.getId())
                .name(p.getName())
                .userId((long) p.getUserId())
                .apiKey(p.getApiKey())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static MerchantTransactionResponse merchantTransaction(
            com.sanedge.paymentgateway.pb.merchant.MerchantTransactionResponse p) {
        if (p == null) {
            return null;
        }
        return MerchantTransactionResponse.builder()
                .id((long) p.getId())
                .cardNumber(p.getCardNumber())
                .amount((long) p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .merchantId((long) p.getMerchantId())
                .merchantName(p.getMerchantName())
                .transactionTime(p.getTransactionTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static List<MerchantResponse> merchants(List<com.sanedge.paymentgateway.pb.merchant.MerchantResponse> list) {
        return list.stream().map(ProtoMapper::merchant).toList();
    }

    public static List<MerchantResponseDeleteAt> merchantsDeleteAt(
            List<com.sanedge.paymentgateway.pb.merchant.MerchantResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::merchantDeleteAt).toList();
    }

    public static List<MerchantTransactionResponse> merchantTransactions(
            List<com.sanedge.paymentgateway.pb.merchant.MerchantTransactionResponse> list) {
        return list.stream().map(ProtoMapper::merchantTransaction).toList();
    }

    // ---- saldo ----
    public static SaldoResponse saldo(com.sanedge.paymentgateway.pb.saldo.SaldoResponse p) {
        if (p == null) {
            return null;
        }
        return SaldoResponse.builder()
                .id((long) p.getSaldoId())
                .cardNumber(p.getCardNumber())
                .totalBalance((long) p.getTotalBalance())
                .withdrawAmount((long) p.getWithdrawAmount())
                .withdrawTime(p.getWithdrawTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static SaldoResponseDeleteAt saldoDeleteAt(com.sanedge.paymentgateway.pb.saldo.SaldoResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return SaldoResponseDeleteAt.builder()
                .id((long) p.getSaldoId())
                .cardNumber(p.getCardNumber())
                .totalBalance((long) p.getTotalBalance())
                .withdrawAmount((long) p.getWithdrawAmount())
                .withdrawTime(p.getWithdrawTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static List<SaldoResponse> saldos(List<com.sanedge.paymentgateway.pb.saldo.SaldoResponse> list) {
        return list.stream().map(ProtoMapper::saldo).toList();
    }

    public static List<SaldoResponseDeleteAt> saldosDeleteAt(
            List<com.sanedge.paymentgateway.pb.saldo.SaldoResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::saldoDeleteAt).toList();
    }

    // ---- topup ----
    public static TopupResponse topup(com.sanedge.paymentgateway.pb.topup.TopupResponse p) {
        if (p == null) {
            return null;
        }
        return TopupResponse.builder()
                .id((long) p.getId())
                .cardNumber(p.getCardNumber())
                .topupNo(p.getTopupNo())
                .topupAmount((long) p.getTopupAmount())
                .topupMethod(p.getTopupMethod())
                .topupTime(p.getTopupTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static TopupResponseDeleteAt topupDeleteAt(com.sanedge.paymentgateway.pb.topup.TopupResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return TopupResponseDeleteAt.builder()
                .id((long) p.getId())
                .cardNumber(p.getCardNumber())
                .topupNo(p.getTopupNo())
                .topupAmount((long) p.getTopupAmount())
                .topupMethod(p.getTopupMethod())
                .topupTime(p.getTopupTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static List<TopupResponse> topups(List<com.sanedge.paymentgateway.pb.topup.TopupResponse> list) {
        return list.stream().map(ProtoMapper::topup).toList();
    }

    public static List<TopupResponseDeleteAt> topupsDeleteAt(
            List<com.sanedge.paymentgateway.pb.topup.TopupResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::topupDeleteAt).toList();
    }

    // ---- transaction ----
    public static TransactionResponse transaction(com.sanedge.paymentgateway.pb.transaction.TransactionResponse p) {
        if (p == null) {
            return null;
        }
        return TransactionResponse.builder()
                .id((long) p.getId())
                .transactionNo(p.getTransactionNo())
                .cardNumber(p.getCardNumber())
                .amount((long) p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .merchantId((long) p.getMerchantId())
                .transactionTime(p.getTransactionTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static TransactionResponseDeleteAt transactionDeleteAt(
            com.sanedge.paymentgateway.pb.transaction.TransactionResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return TransactionResponseDeleteAt.builder()
                .id((long) p.getId())
                .transactionNo(p.getTransactionNo())
                .cardNumber(p.getCardNumber())
                .amount((long) p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .merchantId((long) p.getMerchantId())
                .transactionTime(p.getTransactionTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static List<TransactionResponse> transactions(
            List<com.sanedge.paymentgateway.pb.transaction.TransactionResponse> list) {
        return list.stream().map(ProtoMapper::transaction).toList();
    }

    public static List<TransactionResponseDeleteAt> transactionsDeleteAt(
            List<com.sanedge.paymentgateway.pb.transaction.TransactionResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::transactionDeleteAt).toList();
    }

    // ---- transfer ----
    public static TransferResponse transfer(com.sanedge.paymentgateway.pb.transfer.TransferResponse p) {
        if (p == null) {
            return null;
        }
        return TransferResponse.builder()
                .id((long) p.getId())
                .transferNo(p.getTransferNo())
                .transferFrom(p.getTransferFrom())
                .transferTo(p.getTransferTo())
                .transferAmount((long) p.getTransferAmount())
                .transferTime(p.getTransferTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static TransferResponseDeleteAt transferDeleteAt(
            com.sanedge.paymentgateway.pb.transfer.TransferResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return TransferResponseDeleteAt.builder()
                .id((long) p.getId())
                .transferNo(p.getTransferNo())
                .transferFrom(p.getTransferFrom())
                .transferTo(p.getTransferTo())
                .transferAmount((long) p.getTransferAmount())
                .transferTime(p.getTransferTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static List<TransferResponse> transfers(List<com.sanedge.paymentgateway.pb.transfer.TransferResponse> list) {
        return list.stream().map(ProtoMapper::transfer).toList();
    }

    public static List<TransferResponseDeleteAt> transfersDeleteAt(
            List<com.sanedge.paymentgateway.pb.transfer.TransferResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::transferDeleteAt).toList();
    }

    // ---- withdraw ----
    public static WithdrawResponse withdraw(com.sanedge.paymentgateway.pb.withdraw.WithdrawResponse p) {
        if (p == null) {
            return null;
        }
        return WithdrawResponse.builder()
                .id((long) p.getWithdrawId())
                .withdrawNo(p.getWithdrawNo())
                .cardNumber(p.getCardNumber())
                .withdrawAmount((long) p.getWithdrawAmount())
                .withdrawTime(p.getWithdrawTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public static WithdrawResponseDeleteAt withdrawDeleteAt(
            com.sanedge.paymentgateway.pb.withdraw.WithdrawResponseDeleteAt p) {
        if (p == null) {
            return null;
        }
        return WithdrawResponseDeleteAt.builder()
                .id((long) p.getWithdrawId())
                .withdrawNo(p.getWithdrawNo())
                .cardNumber(p.getCardNumber())
                .withdrawAmount((long) p.getWithdrawAmount())
                .withdrawTime(p.getWithdrawTime())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    public static List<WithdrawResponse> withdraws(List<com.sanedge.paymentgateway.pb.withdraw.WithdrawResponse> list) {
        return list.stream().map(ProtoMapper::withdraw).toList();
    }

    public static List<WithdrawResponseDeleteAt> withdrawsDeleteAt(
            List<com.sanedge.paymentgateway.pb.withdraw.WithdrawResponseDeleteAt> list) {
        return list.stream().map(ProtoMapper::withdrawDeleteAt).toList();
    }
}
