package com.sanedge.paymentgateway.transfer.service.impl;

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
import com.sanedge.paymentgateway.transfer.domain.requests.FindAllTransfers;
import com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse;
import com.sanedge.paymentgateway.transfer.domain.responses.TransferResponseDeleteAt;
import com.sanedge.paymentgateway.transfer.models.Transfer;
import com.sanedge.paymentgateway.transfer.repository.TransferQueryRepository;
import com.sanedge.paymentgateway.transfer.service.TransferQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransferQueryImplService implements TransferQueryService {
        private final TransferQueryRepository transferQueryRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<TransferResponse>> findAll(FindAllTransfers req) {
                return tracingMetrics.traceAndMeasure("findAllTransfers", "find_all", () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String search = req.getSearch() != null ? req.getSearch() : "";

                                log.info("🔍 Searching all transfers | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                                search.isEmpty() ? "None" : search);

                                Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
                                Page<Transfer> pageResult = transferQueryRepository.findTransfers(search, pageable);

                                List<TransferResponse> data = pageResult.getContent().stream()
                                                .map(TransferResponse::from)
                                                .toList();

                                log.info("✅ Found {} transfers", data.size());

                                return ApiResponsePagination.<List<TransferResponse>>builder()
                                                .status("success")
                                                .message("Transfers retrieved successfully")
                                                .data(data)
                                                .pagination(PaginationMeta.fromSpringPage(pageResult))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch transfers", e);
                                return ApiResponsePagination.<List<TransferResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch transfers")
                                                .build();
                        }
                });

        }

        @Override
        public ApiResponse<TransferResponse> findById(Long transferId) {
                Attributes attrs = Attributes.builder()
                                .put("transferId", String.valueOf(transferId))
                                .build();

                return tracingMetrics.traceAndMeasure("findTransferById", "find_by_id", attrs, () -> {
                        Transfer transfer = transferQueryRepository.findById(transferId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Transfer not found with id " + transferId));
                        return ApiResponse.<TransferResponse>builder()
                                        .status("success")
                                        .message("Transfer retrieved successfully")
                                        .data(TransferResponse.from(transfer))
                                        .build();
                });
        }

        @Override
        public ApiResponsePagination<List<TransferResponseDeleteAt>> findByActive(FindAllTransfers req) {
                return tracingMetrics.traceAndMeasure("findActiveTransfers", "find_active", () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String search = req.getSearch() != null ? req.getSearch() : "";

                                Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
                                Page<Transfer> pageResult = transferQueryRepository.findActiveTransfers(search, pageable);

                                List<TransferResponseDeleteAt> data = pageResult.getContent().stream()
                                                .map(TransferResponseDeleteAt::from)
                                                .toList();

                                return ApiResponsePagination.<List<TransferResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Active transfers retrieved successfully")
                                                .data(data)
                                                .pagination(PaginationMeta.fromSpringPage(pageResult))
                                                .build();

                        } catch (Exception e) {
                                return ApiResponsePagination.<List<TransferResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch active transfers")
                                                .build();
                        }
                });

        }

        @Override
        public ApiResponsePagination<List<TransferResponseDeleteAt>> findByTrashed(FindAllTransfers req) {
                return tracingMetrics.traceAndMeasure("findTrashedTransfers", "find_trashed", () -> {
                        try {
                                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                String search = req.getSearch() != null ? req.getSearch() : "";

                                Pageable pageable = PageRequest.of(page, pageSize, Sort.by("deletedAt").descending());
                                Page<Transfer> pageResult = transferQueryRepository.findTrashedTransfers(search, pageable);

                                List<TransferResponseDeleteAt> data = pageResult.getContent().stream()
                                                .map(TransferResponseDeleteAt::from)
                                                .toList();

                                return ApiResponsePagination.<List<TransferResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Trashed transfers retrieved successfully")
                                                .data(data)
                                                .pagination(PaginationMeta.fromSpringPage(pageResult))
                                                .build();

                        } catch (Exception e) {
                                return ApiResponsePagination.<List<TransferResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch trashed transfers")
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<List<TransferResponse>> findByTransferFrom(String transferFrom) {
                Attributes attrs = Attributes.builder()
                                .put("transferFrom", transferFrom != null ? transferFrom : "")
                                .build();

                return tracingMetrics.traceAndMeasure("findTransfersByTransferFrom", "find_by_transfer_from", attrs, () -> {
                        try {
                                List<Transfer> transfers = transferQueryRepository.findTransfersBySourceCard(transferFrom);
                                List<TransferResponse> data = transfers.stream()
                                                .map(TransferResponse::from)
                                                .toList();
                                return ApiResponse.<List<TransferResponse>>builder()
                                                .status("success")
                                                .message("Transfers by sender retrieved successfully")
                                                .data(data)
                                                .build();

                        } catch (Exception e) {
                                return ApiResponse.<List<TransferResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch transfers")
                                                .build();

                        }
                });

        }

        @Override
        public ApiResponse<List<TransferResponse>> findByTransferTo(String transferTo) {
                Attributes attrs = Attributes.builder()
                                .put("transferTo", transferTo != null ? transferTo : "")
                                .build();

                return tracingMetrics.traceAndMeasure("findTransfersByTransferTo", "find_by_transfer_to", attrs, () -> {
                        try {
                                List<Transfer> transfers = transferQueryRepository.findTransfersByDestinationCard(transferTo);
                                List<TransferResponse> data = transfers.stream()
                                                .map(TransferResponse::from)
                                                .toList();
                                return ApiResponse.<List<TransferResponse>>builder()
                                                .status("success")
                                                .message("Transfers by receiver retrieved successfully")
                                                .data(data)
                                                .build();

                        } catch (Exception e) {
                                return ApiResponse.<List<TransferResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch transfers")
                                                .build();
                        }
                });

        }
}
