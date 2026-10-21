package com.sanedge.paymentgateway.topup.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.domain.responses.api.PaginationMeta;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.topup.domain.requests.FindAllTopups;
import com.sanedge.paymentgateway.topup.domain.requests.FindAllTopupsByCardNumber;
import com.sanedge.paymentgateway.topup.domain.responses.TopupResponse;
import com.sanedge.paymentgateway.topup.domain.responses.TopupResponseDeleteAt;
import com.sanedge.paymentgateway.topup.models.Topup;
import com.sanedge.paymentgateway.topup.repository.TopupQueryRepository;
import com.sanedge.paymentgateway.topup.service.TopupQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TopupQueryImplService implements TopupQueryService {
        private final TopupQueryRepository topupQueryRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<TopupResponse>> findAll(FindAllTopups req) {
                return tracingMetrics.traceAndMeasure("findAllTopups", "find_all_topups", () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String search = req.getSearch() != null ? req.getSearch() : "";

                                log.info("🔍 Searching all topups | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                                search.isEmpty() ? "None" : search);

                                Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
                                Page<Topup> pageResult = topupQueryRepository.findTopups(search, pageable);

                                List<TopupResponse> data = pageResult.getContent().stream()
                                                .map(TopupResponse::from)
                                                .toList();

                                log.info("✅ Found {} topups", data.size());

                                return ApiResponsePagination.<List<TopupResponse>>builder()
                                                .status("success")
                                                .data(data)
                                                .message("Found " + data.size() + " topups")
                                                .pagination(PaginationMeta.fromSpringPage(pageResult))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch all topups", e);
                                return ApiResponsePagination.<List<TopupResponse>>builder()
                                                .status("error")
                                                .data(Collections.emptyList())
                                                .message("Failed to fetch all topups")
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<TopupResponse>> findAllByCardNumber(FindAllTopupsByCardNumber req) {
                Attributes attrs = Attributes.builder()
                                .put("cardNumber", req.getCardNumber() != null ? req.getCardNumber() : "")
                                .build();

                return tracingMetrics.traceAndMeasure("findAllTopupsByCardNumber", "find_all_topups_by_card_number",
                                attrs, () -> {
                                        try {
                                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                                String cardNumber = req.getCardNumber();
                                                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty())
                                                                ? req.getSearch()
                                                                : "";

                                                log.info("🔍 Searching topups by card number: {} | Page: {}, Size: {}, Search: {}",
                                                                cardNumber, page + 1, pageSize, keyword);

                                                Pageable pageable = PageRequest.of(page, pageSize);
                                                Page<Topup> pageResult = topupQueryRepository.findTopupByCard(cardNumber,
                                                                keyword, pageable);

                                                List<TopupResponse> data = pageResult.getContent().stream()
                                                                .map(TopupResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} topups for card {}", data.size(), cardNumber);

                                                return ApiResponsePagination.<List<TopupResponse>>builder()
                                                                .status("success")
                                                                .data(data)
                                                                .message("Found " + data.size() + " topups for card="
                                                                                + cardNumber)
                                                                .pagination(PaginationMeta.fromSpringPage(pageResult))
                                                                .build();
                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch topups for card={}", req.getCardNumber(), e);
                                                return ApiResponsePagination.<List<TopupResponse>>builder()
                                                                .status("error")
                                                                .data(Collections.emptyList())
                                                                .pagination(null)
                                                                .message("Failed to fetch topups for card="
                                                                                + req.getCardNumber())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponsePagination<List<TopupResponseDeleteAt>> findActive(FindAllTopups req) {
                return tracingMetrics.traceAndMeasure("findActiveTopups", "find_active_topups", () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String search = req.getSearch() != null ? req.getSearch() : "";

                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Topup> pageResult = topupQueryRepository.findActiveTopups(search, pageable);

                                List<TopupResponseDeleteAt> data = pageResult.getContent().stream()
                                                .map(TopupResponseDeleteAt::from)
                                                .toList();

                                return ApiResponsePagination.<List<TopupResponseDeleteAt>>builder()
                                                .status("success")
                                                .data(data)
                                                .message("Found " + data.size() + " active topups")
                                                .pagination(PaginationMeta.fromSpringPage(pageResult))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch active topups", e);
                                return ApiResponsePagination.<List<TopupResponseDeleteAt>>builder()
                                                .status("error")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .message("Failed to fetch active topups")
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<TopupResponseDeleteAt>> findTrashed(FindAllTopups req) {
                return tracingMetrics.traceAndMeasure("findTrashedTopups", "find_trashed_topups", () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String search = req.getSearch() != null ? req.getSearch() : "";

                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Topup> pageResult = topupQueryRepository.findTrashedTopups(search, pageable);

                                List<TopupResponseDeleteAt> data = pageResult.getContent().stream()
                                                .map(TopupResponseDeleteAt::from)
                                                .toList();

                                return ApiResponsePagination.<List<TopupResponseDeleteAt>>builder()
                                                .status("success")
                                                .data(data)
                                                .message("Found " + data.size() + " trashed topups")
                                                .pagination(PaginationMeta.fromSpringPage(pageResult))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch trashed topups", e);
                                return ApiResponsePagination.<List<TopupResponseDeleteAt>>builder()
                                                .status("error")
                                                .data(Collections.emptyList())
                                                .message("Failed to fetch trashed topups")
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<List<TopupResponse>> findByCard(String cardNumber) {
                Attributes attrs = Attributes.builder()
                                .put("cardNumber", cardNumber != null ? cardNumber : "")
                                .build();

                return tracingMetrics.traceAndMeasure("findTopupsByCard", "find_topups_by_card", attrs, () -> {
                        try {
                                List<Topup> topups = topupQueryRepository.findByCardNumber(cardNumber);
                                List<TopupResponse> data = topups.stream()
                                                .map(TopupResponse::from)
                                                .toList();

                                log.info("✅ Retrieved {} topups for card {}", data.size(), cardNumber);

                                return ApiResponse.<List<TopupResponse>>builder()
                                                .status("success")
                                                .message("Found " + data.size() + " topups for card=" + cardNumber)
                                                .data(data)
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch topups for card={}", cardNumber, e);
                                return ApiResponse.<List<TopupResponse>>builder()
                                                .status("error")
                                                .data(Collections.emptyList())
                                                .message("Failed to fetch topups for card=" + cardNumber)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<TopupResponse> findById(Long topupId) {
                Attributes attrs = Attributes.builder()
                                .put("topupId", String.valueOf(topupId))
                                .build();

                return tracingMetrics.traceAndMeasure("findTopupById", "find_topup_by_id", attrs, () -> {
                        try {
                                Topup topup = topupQueryRepository.findById(topupId)
                                                .orElseThrow(() -> {
                                                        log.error("❌ Topup not found id={}", topupId);
                                                        return new ResourceNotFoundException("Topup not found");
                                                });

                                log.info("✅ Retrieved topup id={}", topupId);

                                return ApiResponse.<TopupResponse>builder()
                                                .status("success")
                                                .data(TopupResponse.from(topup))
                                                .message("Found topup id=" + topupId)
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch topup id={}", topupId, e);
                                return ApiResponse.<TopupResponse>builder()
                                                .status("error")
                                                .data(null)
                                                .message("Failed to fetch topup id=" + topupId)
                                                .build();
                        }
                });
        }

}
