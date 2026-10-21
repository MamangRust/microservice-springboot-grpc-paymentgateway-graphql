package com.sanedge.paymentgateway.transaction.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.sanedge.paymentgateway.common.adapter.card.CardPort;
import com.sanedge.paymentgateway.common.adapter.fraud.FraudPort;
import com.sanedge.paymentgateway.common.adapter.merchant.MerchantPort;
import com.sanedge.paymentgateway.common.adapter.model.Card;
import com.sanedge.paymentgateway.common.adapter.model.Merchant;
import com.sanedge.paymentgateway.common.adapter.model.Saldo;
import com.sanedge.paymentgateway.common.adapter.model.SaldoMutationResult;
import com.sanedge.paymentgateway.common.adapter.saldo.SaldoPort;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.common.outbox.OutboxRepository;
import com.sanedge.paymentgateway.transaction.domain.requests.CreateTransactionRequest;
import com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse;
import com.sanedge.paymentgateway.transaction.models.Transaction;
import com.sanedge.paymentgateway.transaction.repository.TransactionCommandRepository;
import com.sanedge.paymentgateway.transaction.repository.TransactionQueryRepository;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.common.Attributes;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionCommandImplServiceTest {

    private static final String CARD_NUMBER = "4111111111111111";
    private static final String MERCHANT_ACCOUNT = "4444444444444444";
    private static final String API_KEY = "api-key-1";

    @Mock
    private TransactionQueryRepository transactionQueryRepository;
    @Mock
    private TransactionCommandRepository transactionCommandRepository;
    @Mock
    private Validator validator;
    @Mock
    private TracingMetrics tracingMetrics;
    @Mock
    private MerchantPort merchantPort;
    @Mock
    private SaldoPort saldoPort;
    @Mock
    private CardPort cardPort;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private FraudPort fraudPort;

    private TransactionCommandImplService service;

    @BeforeEach
    void setUp() {
        service = new TransactionCommandImplService(transactionQueryRepository, transactionCommandRepository,
                validator, tracingMetrics, merchantPort, saldoPort, cardPort, outboxRepository, fraudPort);
        ReflectionTestUtils.setField(service, "notificationEmail", "admin@example.com");

        when(validator.validate(any())).thenReturn(Set.of());
        when(tracingMetrics.traceAndMeasure(anyString(), anyString(), any(Attributes.class), any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(3)).get());

        Merchant merchant = new Merchant(1, "Merchant A", API_KEY, "active", 99, Instant.now(), Instant.now());
        Card customerCard = card(5, 42, CARD_NUMBER);
        Card merchantCard = card(7, 99, MERCHANT_ACCOUNT);

        when(merchantPort.findByApiKey(API_KEY)).thenReturn(merchant);
        when(cardPort.findCardByCardNumber(CARD_NUMBER)).thenReturn(customerCard);
        when(cardPort.findCardByUserId(99)).thenReturn(merchantCard);
        when(saldoPort.findByCardNumber(CARD_NUMBER))
                .thenReturn(new Saldo(1, CARD_NUMBER, 1_000_000, null, null, null, null));
        when(saldoPort.findByCardNumber(MERCHANT_ACCOUNT))
                .thenReturn(new Saldo(2, MERCHANT_ACCOUNT, 0, null, null, null, null));
        when(saldoPort.updateSaldoBalance(any()))
                .thenReturn(new SaldoMutationResult(1, CARD_NUMBER, 0));
        when(transactionCommandRepository.save(any())).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setTransactionId(1L);
            return transaction;
        });
        when(transactionCommandRepository.updateTransactionStatus(anyLong(), anyString())).thenReturn(null);
    }

    @Test
    void flaggedAsFraud_rejectsTransaction_andSkippedSaldoDebit() {
        when(fraudPort.detectFraud(any())).thenReturn(new FraudPort.FraudResult("txn-1", 0.95, true, "high risk"));

        ApiResponse<TransactionResponse> response = service.create(API_KEY, request(150000L));

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getData()).isNull();
        verify(saldoPort, never()).updateSaldoBalance(any());
        verify(transactionCommandRepository, never()).save(any());
    }

    @Test
    void aiSecurityDown_failsOpen_andTransactionSucceeds() {
        when(fraudPort.detectFraud(any())).thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        ApiResponse<TransactionResponse> response = service.create(API_KEY, request(150000L));

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getData()).isNotNull();
        // customer debit + merchant credit
        verify(saldoPort, org.mockito.Mockito.times(2)).updateSaldoBalance(any());
        verify(transactionCommandRepository).save(any());
    }

    @Test
    void safeVerdict_transactionSucceeds() {
        when(fraudPort.detectFraud(any())).thenReturn(new FraudPort.FraudResult("txn-1", 0.1, false, "normal"));

        ApiResponse<TransactionResponse> response = service.create(API_KEY, request(150000L));

        assertThat(response.getStatus()).isEqualTo("success");
        verify(transactionCommandRepository).save(any());
    }

    private static CreateTransactionRequest request(long amount) {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setCardNumber(CARD_NUMBER);
        req.setAmount(amount);
        req.setPaymentMethod("CASH");
        req.setMerchantId(1L);
        return req;
    }

    private static Card card(int id, int userId, String cardNumber) {
        return new Card(id, userId, cardNumber, "CREDIT", "12/27", "123", "VISA", "user@example.com",
                Instant.now(), Instant.now());
    }
}
