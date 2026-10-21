package com.sanedge.paymentgateway.saldo.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.domain.responses.api.PaginationMeta;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.saldo.domain.requests.FindAllSaldos;
import com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse;
import com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt;
import com.sanedge.paymentgateway.saldo.models.Saldo;
import com.sanedge.paymentgateway.saldo.repository.SaldoQueryRepository;
import com.sanedge.paymentgateway.saldo.service.SaldoQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class SaldoQueryImplService implements SaldoQueryService {
        private final SaldoQueryRepository saldoQueryRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<SaldoResponse>> findAll(FindAllSaldos req) {
                return tracingMetrics.traceAndMeasure("findAllSaldos", "find_all_saldos", () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                                log.info("🔍 Searching all saldos | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                                keyword.isEmpty() ? "None" : keyword);

                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Saldo> saldoPage = saldoQueryRepository.findSaldos(keyword, pageable);

                                List<SaldoResponse> saldoResponses = saldoPage.getContent()
                                                .stream()
                                                .map(SaldoResponse::from)
                                                .toList();

                                log.info("✅ Found {} saldos", saldoResponses.size());

                                return ApiResponsePagination.<List<SaldoResponse>>builder()
                                                .status("success")
                                                .message("Get all saldos success")
                                                .data(saldoResponses)
                                                .pagination(PaginationMeta.fromSpringPage(saldoPage))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch all saldos", e);
                                return ApiResponsePagination.<List<SaldoResponse>>builder()
                                                .status("error")
                                                .message("Failed to get all saldos")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<SaldoResponseDeleteAt>> findActive(FindAllSaldos req) {
                return tracingMetrics.traceAndMeasure("findActiveSaldos", "find_active_saldos", () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                                log.info("🔍 Searching active saldos | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                                keyword.isEmpty() ? "None" : keyword);

                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Saldo> saldoPage = saldoQueryRepository.findActiveSaldos(keyword, pageable);

                                List<SaldoResponseDeleteAt> saldoResponses = saldoPage.getContent()
                                                .stream()
                                                .map(SaldoResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} active saldos", saldoResponses.size());

                                return ApiResponsePagination.<List<SaldoResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Get active saldos success")
                                                .data(saldoResponses)
                                                .pagination(PaginationMeta.fromSpringPage(saldoPage))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch active saldos", e);
                                return ApiResponsePagination.<List<SaldoResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to get active saldos")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<SaldoResponseDeleteAt>> findTrashed(FindAllSaldos req) {
                return tracingMetrics.traceAndMeasure("findTrashedSaldos", "find_trashed_saldos", () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                                log.info("🔍 Searching trashed saldos | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                                keyword.isEmpty() ? "None" : keyword);

                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Saldo> saldoPage = saldoQueryRepository.findTrashedSaldos(keyword, pageable);

                                List<SaldoResponseDeleteAt> saldoResponses = saldoPage.getContent()
                                                .stream()
                                                .map(SaldoResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} trashed saldos", saldoResponses.size());

                                return ApiResponsePagination.<List<SaldoResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Get trashed saldos success")
                                                .data(saldoResponses)
                                                .pagination(PaginationMeta.fromSpringPage(saldoPage))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch trashed saldos", e);
                                return ApiResponsePagination.<List<SaldoResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to get trashed saldos")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<SaldoResponse> findByCard(String cardNumber) {
                Attributes attrs = Attributes.builder()
                                .put("cardNumber", cardNumber != null ? cardNumber : "")
                                .build();

                return tracingMetrics.traceAndMeasure("findSaldoByCard", "find_saldo_by_card", attrs, () -> {
                        try {
                                log.info("🔍 Fetching saldo by card number: {}", cardNumber);

                                Saldo saldo = saldoQueryRepository.findByCardNumber(cardNumber)
                                                .orElseThrow(() -> {
                                                        log.error("Saldo not found for card: {}", cardNumber);
                                                        return new ResourceNotFoundException("Saldo not found");
                                                });

                                log.info("✅ Saldo fetched successfully for card number {}", cardNumber);
                                return ApiResponse.<SaldoResponse>builder()
                                                .status("success")
                                                .message("Get saldo success")
                                                .data(SaldoResponse.from(saldo))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch saldo for card number {}", cardNumber, e);
                                return ApiResponse.<SaldoResponse>builder()
                                                .status("error")
                                                .message("Failed to get saldo")
                                                .data(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<SaldoResponse> findById(Long id) {
                Attributes attrs = Attributes.builder()
                                .put("saldo.id", String.valueOf(id))
                                .build();

                return tracingMetrics.traceAndMeasure("findSaldoById", "find_saldo_by_id", attrs, () -> {
                        try {
                                log.info("🔍 Fetching saldo by id: {}", id);

                                Saldo saldo = saldoQueryRepository.findById(id)
                                                .orElseThrow(() -> {
                                                        log.error("Saldo not found with id: {}", id);
                                                        return new ResourceNotFoundException("Saldo not found");
                                                });

                                log.info("✅ Saldo fetched successfully for id {}", id);
                                return ApiResponse.<SaldoResponse>builder()
                                                .status("success")
                                                .message("Get saldo success")
                                                .data(SaldoResponse.from(saldo))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch saldo for id {}", id, e);
                                return ApiResponse.<SaldoResponse>builder()
                                                .status("error")
                                                .message("Failed to get saldo")
                                                .data(null)
                                                .build();
                        }
                });
        }
}
