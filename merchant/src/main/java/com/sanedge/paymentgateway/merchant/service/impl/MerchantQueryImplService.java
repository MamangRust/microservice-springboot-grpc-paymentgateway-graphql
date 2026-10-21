package com.sanedge.paymentgateway.merchant.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.merchant.domain.requests.FindAllMerchants;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.domain.responses.api.PaginationMeta;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.merchant.models.Merchant;
import com.sanedge.paymentgateway.merchant.repository.MerchantQueryRepository;
import com.sanedge.paymentgateway.merchant.service.MerchantQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantQueryImplService implements MerchantQueryService {
        private final MerchantQueryRepository merchantQueryRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<MerchantResponse>> findAll(FindAllMerchants req) {
                Attributes attrs = Attributes.builder()
                                .put("operation", "find_all_merchants")
                                .put("service.name", "merchant-query-service")
                                .build();

                return tracingMetrics.traceAndMeasure("findAllMerchants", "find_all_merchants", attrs, () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                                log.info("🔍 Searching all merchants | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                                keyword.isEmpty() ? "None" : keyword);

                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Merchant> merchantPage = merchantQueryRepository.findMerchants(keyword, pageable);

                                List<MerchantResponse> merchantResponses = merchantPage.getContent()
                                                .stream()
                                                .map(MerchantResponse::from)
                                                .toList();

                                log.info("✅ Found {} merchants", merchantResponses.size());

                                return ApiResponsePagination.<List<MerchantResponse>>builder()
                                                .status("success")
                                                .data(merchantResponses)
                                                .message("Found " + merchantResponses.size() + " merchants")
                                                .pagination(PaginationMeta.fromSpringPage(merchantPage))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to retrieve all merchants", e);
                                return ApiResponsePagination.<List<MerchantResponse>>builder()
                                                .status("error")
                                                .data(Collections.emptyList())
                                                .message("Failed to retrieve all merchants")
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<MerchantResponseDeleteAt>> findByActive(FindAllMerchants req) {
                Attributes attrs = Attributes.builder()
                                .put("operation", "find_active_merchants")
                                .put("service.name", "merchant-query-service")
                                .build();

                return tracingMetrics.traceAndMeasure("findActiveMerchants", "find_active_merchants", attrs, () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                                log.info("🔍 Searching active merchants | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                                keyword.isEmpty() ? "None" : keyword);

                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Merchant> merchantPage = merchantQueryRepository.findActiveMerchants(keyword, pageable);

                                List<MerchantResponseDeleteAt> merchantResponses = merchantPage.getContent()
                                                .stream()
                                                .map(MerchantResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} active merchants", merchantResponses.size());

                                return ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                                                .status("success")
                                                .data(merchantResponses)
                                                .message("Found " + merchantResponses.size() + " active merchants")
                                                .pagination(PaginationMeta.fromSpringPage(merchantPage))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to retrieve active merchants", e);
                                return ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                                                .status("error")
                                                .data(Collections.emptyList())
                                                .message("Failed to retrieve active merchants")
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<MerchantResponseDeleteAt>> findByTrashed(FindAllMerchants req) {
                Attributes attrs = Attributes.builder()
                                .put("operation", "find_trashed_merchants")
                                .put("service.name", "merchant-query-service")
                                .build();

                return tracingMetrics.traceAndMeasure("findTrashedMerchants", "find_trashed_merchants", attrs, () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                                log.info("🔍 Searching trashed merchants | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                                keyword.isEmpty() ? "None" : keyword);

                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Merchant> merchantPage = merchantQueryRepository.findTrashedMerchants(keyword, pageable);

                                List<MerchantResponseDeleteAt> merchantResponses = merchantPage.getContent()
                                                .stream()
                                                .map(MerchantResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} trashed merchants", merchantResponses.size());

                                return ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                                                .status("success")
                                                .data(merchantResponses)
                                                .message("Found " + merchantResponses.size() + " trashed merchants")
                                                .pagination(PaginationMeta.fromSpringPage(merchantPage))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to retrieve trashed merchants", e);
                                return ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                                                .status("error")
                                                .data(Collections.emptyList())
                                                .message("Failed to retrieve trashed merchants")
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<MerchantResponse> findById(Long merchantId) {
                Attributes attrs = Attributes.builder()
                                .put("operation", "find_merchant_by_id")
                                .put("service.name", "merchant-query-service")
                                .put("merchant.id", String.valueOf(merchantId))
                                .build();

                return tracingMetrics.traceAndMeasure("findMerchantById", "find_merchant_by_id", attrs, () -> {
                        log.info("🔍 Finding merchant by ID: {}", merchantId);
                        try {
                                Merchant merchant = merchantQueryRepository.findMerchantById(merchantId)
                                                .orElseThrow(() -> {
                                                        log.error("❌ Merchant not found with id {}", merchantId);
                                                        return new ResourceNotFoundException("❌ Merchant not found with id");
                                                });

                                return ApiResponse.<MerchantResponse>builder()
                                                .status("success")
                                                .data(MerchantResponse.from(merchant))
                                                .message("Found merchant with id " + merchantId)
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to find merchant by id={}", merchantId, e);
                                return ApiResponse.<MerchantResponse>builder()
                                                .status("error")
                                                .data(null)
                                                .message("Failed to find merchant with id " + merchantId)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<MerchantResponse> findByApiKey(String apiKey) {
                Attributes attrs = Attributes.builder()
                                .put("operation", "find_merchant_by_api_key")
                                .put("service.name", "merchant-query-service")
                                .build();

                return tracingMetrics.traceAndMeasure("findMerchantByApiKey", "find_merchant_by_api_key", attrs, () -> {
                        log.info("🔑 Finding merchant by API key: {}", apiKey);
                        try {
                                Merchant merchant = merchantQueryRepository.findByApiKey(apiKey)
                                                .orElseThrow(() -> {
                                                        log.error("❌ Merchant not found with API key {}", apiKey);
                                                        return new ResourceNotFoundException("Merchant not found with API key");
                                                });

                                return ApiResponse.<MerchantResponse>builder()
                                                .status("success")
                                                .data(MerchantResponse.from(merchant))
                                                .message("Found merchant with API key")
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to find merchant by apiKey={}", apiKey, e);
                                return ApiResponse.<MerchantResponse>builder()
                                                .status("error")
                                                .data(null)
                                                .message("Failed to find merchant with API key")
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<List<MerchantResponse>> findByUserId(Long userId) {
                Attributes attrs = Attributes.builder()
                                .put("operation", "find_merchants_by_user_id")
                                .put("service.name", "merchant-query-service")
                                .put("user.id", String.valueOf(userId))
                                .build();

                return tracingMetrics.traceAndMeasure("findMerchantsByUserId", "find_merchants_by_user_id", attrs, () -> {
                        log.info("👤 Finding merchants by userId: {}", userId);
                        try {
                                List<Merchant> merchants = merchantQueryRepository.findByUserId(userId);
                                List<MerchantResponse> responses = merchants.stream()
                                                .map(MerchantResponse::from)
                                                .toList();

                                return ApiResponse.<List<MerchantResponse>>builder()
                                                .status("success")
                                                .data(responses)
                                                .message("Found " + responses.size() + " merchants")
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to find merchants by userId={}", userId, e);
                                return ApiResponse.<List<MerchantResponse>>builder()
                                                .status("error")
                                                .data(Collections.emptyList())
                                                .message("Failed to find merchants with userId " + userId)
                                                .build();
                        }
                });
        }
}
