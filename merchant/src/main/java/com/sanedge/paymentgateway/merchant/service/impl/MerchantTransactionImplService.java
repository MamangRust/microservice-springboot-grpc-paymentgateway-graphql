package com.sanedge.paymentgateway.merchant.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.merchant.domain.requests.FindAllMerchants;
import com.sanedge.paymentgateway.merchant.domain.requests.transactions.FindAllMerchantTransactionsByApiKey;
import com.sanedge.paymentgateway.merchant.domain.requests.transactions.FindAllMerchantTransactionsById;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.domain.responses.api.PaginationMeta;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantTransactionResponse;
import com.sanedge.paymentgateway.merchant.models.MerchantTransactions;
import com.sanedge.paymentgateway.merchant.repository.MerchantTransactionRepository;
import com.sanedge.paymentgateway.merchant.service.MerchantTransactionService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantTransactionImplService implements MerchantTransactionService {
        private final MerchantTransactionRepository merchantTransactionRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<MerchantTransactionResponse>> findAll(FindAllMerchants req) {
                Attributes attrs = Attributes.builder()
                                .put("operation", "find_all_merchant_transactions")
                                .put("service.name", "merchant-transaction-service")
                                .build();

                return tracingMetrics.traceAndMeasure("findAllMerchantTransactions", "find_all_merchant_transactions",
                                attrs, () -> {
                                        try {
                                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                                                log.info("🔍 Searching all merchant transactions | Page: {}, Size: {}, Search: {}",
                                                                page + 1, pageSize, keyword.isEmpty() ? "None" : keyword);

                                                Pageable pageable = PageRequest.of(page, pageSize);
                                                Page<MerchantTransactions> merchantPage = merchantTransactionRepository
                                                                .findAllTransactions(keyword, pageable);

                                                List<MerchantTransactionResponse> merchantResponses = merchantPage.getContent()
                                                                .stream()
                                                                .map(MerchantTransactionResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} transactions", merchantResponses.size());

                                                return ApiResponsePagination.<List<MerchantTransactionResponse>>builder()
                                                                .status("success")
                                                                .data(merchantResponses)
                                                                .message("Found " + merchantResponses.size() + " transactions")
                                                                .pagination(PaginationMeta.fromSpringPage(merchantPage))
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to retrieve all merchant transactions", e);
                                                return ApiResponsePagination.<List<MerchantTransactionResponse>>builder()
                                                                .status("error")
                                                                .data(Collections.emptyList())
                                                                .message("Failed to retrieve all merchant transactions")
                                                                .pagination(null)
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponsePagination<List<MerchantTransactionResponse>> findById(FindAllMerchantTransactionsById req) {
                Attributes attrs = Attributes.builder()
                                .put("operation", "find_merchant_transactions_by_id")
                                .put("service.name", "merchant-transaction-service")
                                .put("merchant.id", String.valueOf(req.getMerchantId()))
                                .build();

                return tracingMetrics.traceAndMeasure("findMerchantTransactionsById", "find_merchant_transactions_by_id",
                                attrs, () -> {
                                        try {
                                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                                                log.info("🔍 Searching merchant transactions by merchantId={} | Page: {}, Size: {}",
                                                                req.getMerchantId(), page + 1, pageSize);

                                                Pageable pageable = PageRequest.of(page, pageSize);
                                                Page<MerchantTransactions> merchantPage = merchantTransactionRepository
                                                                .findAllTransactionsByMerchant(
                                                                                req.getMerchantId(), keyword, pageable);

                                                List<MerchantTransactionResponse> merchantResponses = merchantPage.getContent()
                                                                .stream()
                                                                .map(MerchantTransactionResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} transactions for merchantId={}", merchantResponses.size(),
                                                                req.getMerchantId());

                                                return ApiResponsePagination.<List<MerchantTransactionResponse>>builder()
                                                                .status("success")
                                                                .data(merchantResponses)
                                                                .message("Found " + merchantResponses.size() + " transactions")
                                                                .pagination(PaginationMeta.fromSpringPage(merchantPage))
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to retrieve transactions for merchantId={}", req.getMerchantId(), e);
                                                return ApiResponsePagination.<List<MerchantTransactionResponse>>builder()
                                                                .status("error")
                                                                .data(Collections.emptyList())
                                                                .message("Failed to retrieve transactions for merchantId="
                                                                                + req.getMerchantId())
                                                                .pagination(null)
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponsePagination<List<MerchantTransactionResponse>> findByApiKey(
                        FindAllMerchantTransactionsByApiKey req) {
                Attributes attrs = Attributes.builder()
                                .put("operation", "find_merchant_transactions_by_api_key")
                                .put("service.name", "merchant-transaction-service")
                                .build();

                return tracingMetrics.traceAndMeasure("findMerchantTransactionsByApiKey",
                                "find_merchant_transactions_by_api_key", attrs, () -> {
                                        try {
                                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                                                log.info("🔑 Searching merchant transactions by apiKey={} | Page: {}, Size: {}",
                                                                req.getApiKey(), page + 1, pageSize);

                                                Pageable pageable = PageRequest.of(page, pageSize);
                                                Page<MerchantTransactions> merchantPage = merchantTransactionRepository
                                                                .findAllTransactionsByApiKey(
                                                                                req.getApiKey(), keyword, pageable);

                                                List<MerchantTransactionResponse> merchantResponses = merchantPage.getContent()
                                                                .stream()
                                                                .map(MerchantTransactionResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} transactions for apiKey={}", merchantResponses.size(), req.getApiKey());

                                                return ApiResponsePagination.<List<MerchantTransactionResponse>>builder()
                                                                .status("success")
                                                                .data(merchantResponses)
                                                                .message("Found " + merchantResponses.size() + " transactions")
                                                                .pagination(PaginationMeta.fromSpringPage(merchantPage))
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to retrieve transactions for apiKey={}", req.getApiKey(), e);
                                                return ApiResponsePagination.<List<MerchantTransactionResponse>>builder()
                                                                .status("error")
                                                                .data(Collections.emptyList())
                                                                .message("Failed to retrieve transactions for apiKey")
                                                                .pagination(null)
                                                                .build();
                                        }
                                });
        }
}
