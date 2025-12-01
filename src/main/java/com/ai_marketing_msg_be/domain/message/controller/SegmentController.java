package com.ai_marketing_msg_be.domain.message.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.message.dto.GetSegmentDetailResponse;
import com.ai_marketing_msg_be.domain.message.dto.GetSegmentListResponse;
import com.ai_marketing_msg_be.domain.message.service.SegmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 고객 세그먼트 REST API Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Segment", description = "고객 세그먼트 관리 API")
public class SegmentController {

    private final SegmentService segmentService;

    /**
     * 고객 세그먼트 목록 조회
     */
    @GetMapping("/admin/customer-segments")
    @Operation(summary = "고객 세그먼트 목록 조회", description = "모든 고객 세그먼트 목록을 조회합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<GetSegmentListResponse>> getSegmentList(
            HttpServletRequest request) {

        log.info("GET /admin/customer-segments");
        GetSegmentListResponse response = segmentService.getSegmentList();

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }

    /**
     * 고객 세그먼트 상세 조회
     */
    @GetMapping("/admin/customer-segments/{segmentId}")
    @Operation(summary = "고객 세그먼트 상세 조회", description = "특정 고객 세그먼트의 상세 정보를 조회합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<GetSegmentDetailResponse>> getSegmentDetail(
            @Parameter(description = "세그먼트 ID", example = "1")
            @PathVariable Long segmentId,
            HttpServletRequest request) {

        log.info("GET /admin/customer-segments/{} - segmentId: {}", segmentId, segmentId);
        GetSegmentDetailResponse response = segmentService.getSegmentDetail(segmentId);

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }
}
