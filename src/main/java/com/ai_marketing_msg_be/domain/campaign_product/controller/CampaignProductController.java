package com.ai_marketing_msg_be.domain.campaign_product.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.campaign_product.dto.AddProductToCampaignRequest;
import com.ai_marketing_msg_be.domain.campaign_product.dto.AddProductToCampaignResponse;
import com.ai_marketing_msg_be.domain.campaign_product.dto.CampaignProductDto;
import com.ai_marketing_msg_be.domain.campaign_product.dto.RemoveProductFromCampaignResponse;
import com.ai_marketing_msg_be.domain.campaign_product.service.CampaignProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 캠페인-상품 매핑 REST API Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "CampaignProduct", description = "캠페인-상품 매핑 API")
public class CampaignProductController {

    private final CampaignProductService campaignProductService;

    /**
     * 캠페인에 상품 추가
     */
    @PostMapping("/admin/campaigns/{campaignId}/products")
    @Operation(summary = "캠페인에 상품 추가", description = "특정 캠페인에 상품을 추가합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<AddProductToCampaignResponse>> addProductToCampaign(
            @Parameter(description = "캠페인 ID", example = "1")
            @PathVariable Long campaignId,
            @Valid @RequestBody AddProductToCampaignRequest request,
            HttpServletRequest httpRequest) {

        log.info("POST /admin/campaigns/{}/products - productId: {}", campaignId, request.getProductId());
        AddProductToCampaignResponse response = campaignProductService.addProductToCampaign(
                campaignId, request.getProductId());

        return ResponseEntity.status(201)
                .body(ApiResponse.created(response, httpRequest.getRequestURI()));
    }

    /**
     * 캠페인에서 상품 제거
     */
    @DeleteMapping("/admin/campaigns/{campaignId}/products/{productId}")
    @Operation(summary = "캠페인에서 상품 제거", description = "특정 캠페인에서 상품을 제거합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<RemoveProductFromCampaignResponse>> removeProductFromCampaign(
            @Parameter(description = "캠페인 ID", example = "1")
            @PathVariable Long campaignId,
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,
            HttpServletRequest request) {

        log.info("DELETE /admin/campaigns/{}/products/{}", campaignId, productId);
        RemoveProductFromCampaignResponse response = campaignProductService.removeProductFromCampaign(
                campaignId, productId);

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }

    /**
     * 특정 캠페인의 모든 상품 조회
     */
    @GetMapping("/campaigns/{campaignId}/products")
    @Operation(summary = "캠페인의 상품 조회", description = "특정 캠페인에 등록된 모든 상품을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CampaignProductDto>>> getProductsByCampaignId(
            @Parameter(description = "캠페인 ID", example = "1")
            @PathVariable Long campaignId,
            HttpServletRequest request) {

        log.info("GET /campaigns/{}/products", campaignId);
        List<CampaignProductDto> response = campaignProductService.getProductsByCampaignId(campaignId);

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }

    /**
     * 특정 상품이 포함된 모든 캠페인 조회
     */
    @GetMapping("/products/{productId}/campaigns")
    @Operation(summary = "상품이 포함된 캠페인 조회", description = "특정 상품이 포함된 모든 캠페인을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CampaignProductDto>>> getCampaignsByProductId(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,
            HttpServletRequest request) {

        log.info("GET /products/{}/campaigns", productId);
        List<CampaignProductDto> response = campaignProductService.getCampaignsByProductId(productId);

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }

    /**
     * 특정 캠페인의 상품 개수 조회
     */
    @GetMapping("/campaigns/{campaignId}/products/count")
    @Operation(summary = "캠페인의 상품 개수 조회", description = "특정 캠페인에 등록된 상품의 개수를 조회합니다.")
    public ResponseEntity<ApiResponse<Long>> countProductsByCampaignId(
            @Parameter(description = "캠페인 ID", example = "1")
            @PathVariable Long campaignId,
            HttpServletRequest request) {

        log.info("GET /campaigns/{}/products/count", campaignId);
        long count = campaignProductService.countProductsByCampaignId(campaignId);

        return ResponseEntity.ok(ApiResponse.ok(count, request.getRequestURI()));
    }

    /**
     * 특정 상품이 포함된 캠페인 개수 조회
     */
    @GetMapping("/products/{productId}/campaigns/count")
    @Operation(summary = "상품이 포함된 캠페인 개수 조회", description = "특정 상품이 포함된 캠페인의 개수를 조회합니다.")
    public ResponseEntity<ApiResponse<Long>> countCampaignsByProductId(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,
            HttpServletRequest request) {

        log.info("GET /products/{}/campaigns/count", productId);
        long count = campaignProductService.countCampaignsByProductId(productId);

        return ResponseEntity.ok(ApiResponse.ok(count, request.getRequestURI()));
    }
}
