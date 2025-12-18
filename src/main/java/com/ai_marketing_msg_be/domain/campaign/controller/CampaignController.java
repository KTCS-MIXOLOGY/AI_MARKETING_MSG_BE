package com.ai_marketing_msg_be.domain.campaign.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.common.dto.PageResponse;
import com.ai_marketing_msg_be.domain.campaign.dto.*;
import com.ai_marketing_msg_be.domain.campaign.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Campaign", description = "캠페인 관리 API")
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * 캠페인 목록 조회
     * GET /campaigns
     */
    @GetMapping("/campaigns")
    @Operation(summary = "캠페인 목록 조회", description = "모든 캠페인 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<CampaignDto>>> getCampaignList(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        log.info("GET /campaigns - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<CampaignDto> response = campaignService.getCampaignList(pageable);

        return ResponseEntity.ok(
                ApiResponse.ok(response, request.getRequestURI())
        );
    }

    /**
     * 캠페인 상세 조회
     * GET /campaigns/{campaignId}
     */
    @GetMapping("/campaigns/{campaignId}")
    @Operation(summary = "캠페인 상세 조회", description = "특정 캠페인의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<CampaignDto>> getCampaignDetail(
            @Parameter(description = "캠페인 ID") @PathVariable Long campaignId,
            HttpServletRequest request
    ) {
        log.info("GET /campaigns/{} - campaignId: {}", campaignId, campaignId);

        CampaignDto response = campaignService.getCampaignDetail(campaignId);

        return ResponseEntity.ok(
                ApiResponse.ok(response, request.getRequestURI())
        );
    }

    /**
     * 캠페인 생성 (Admin)
     * POST /admin/campaigns
     */
    @PostMapping("/admin/campaigns")
    @Operation(summary = "캠페인 생성", description = "새로운 캠페인을 생성합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<CreateCampaignResponse>> createCampaign(
            @Valid @RequestBody CreateCampaignRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("POST /admin/campaigns - request: {}", request);

        // TODO: 실제로는 Spring Security에서 인증된 사용자 ID를 가져와야 함
        Long userId = 1L; // 임시 하드코딩

        CreateCampaignResponse response = campaignService.createCampaign(request, userId);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.created(response, httpRequest.getRequestURI()));
    }

    /**
     * 캠페인 수정 (Admin)
     * PUT /admin/campaigns/{campaignId}
     */
    @PutMapping("/admin/campaigns/{campaignId}")
    @Operation(summary = "캠페인 수정", description = "기존 캠페인 정보를 수정합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<UpdateCampaignResponse>> updateCampaign(
            @Parameter(description = "캠페인 ID") @PathVariable Long campaignId,
            @Valid @RequestBody UpdateCampaignRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("PUT /admin/campaigns/{} - request: {}", campaignId, request);

        // TODO: 실제로는 Spring Security에서 인증된 사용자 ID를 가져와야 함
        Long userId = 1L; // 임시 하드코딩

        UpdateCampaignResponse response = campaignService.updateCampaign(campaignId, request, userId);

        return ResponseEntity.ok(
                ApiResponse.ok(response, httpRequest.getRequestURI())
        );
    }

    /**
     * 캠페인 삭제 (Admin)
     * DELETE /admin/campaigns/{campaignId}
     */
    @DeleteMapping("/admin/campaigns/{campaignId}")
    @Operation(summary = "캠페인 삭제", description = "캠페인을 삭제합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<DeleteCampaignResponse>> deleteCampaign(
            @Parameter(description = "캠페인 ID") @PathVariable Long campaignId,
            HttpServletRequest httpRequest
    ) {
        log.info("DELETE /admin/campaigns/{}", campaignId);

        // TODO: 실제로는 Spring Security에서 인증된 사용자 ID를 가져와야 함
        Long userId = 1L; // 임시 하드코딩

        DeleteCampaignResponse response = campaignService.deleteCampaign(campaignId, userId);

        return ResponseEntity.ok(
                ApiResponse.ok(response, httpRequest.getRequestURI())
        );
    }
}
