package com.ai_marketing_msg_be.domain.customer.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.customer.dto.CampaignRecommendationResponse;
import com.ai_marketing_msg_be.domain.customer.service.CustomerRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/executor/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Recommendation", description = "고객 맞춤 추천 API")
public class CustomerRecommendationController {

    private final CustomerRecommendationService recommendationService;

    @GetMapping("/{customerId}/campaigns/recommendations")
    @PreAuthorize("hasRole('EXECUTOR')")
    @Operation(
            summary = "고객 맞춤 캠페인 추천",
            description = "특정 고객에게 적합한 마케팅 캠페인을 AI 기반으로 추천합니다. " +
                    "productId를 지정하면 해당 상품 마케팅에 최적인 캠페인을 추천합니다."
    )
    public ApiResponse<CampaignRecommendationResponse> recommendCampaigns(
            @Parameter(description = "고객 ID", example = "1", required = true)
            @PathVariable Long customerId,

            @Parameter(description = "타겟 상품 ID (선택사항)", example = "150")
            @RequestParam(required = false) Long productId,

            HttpServletRequest httpRequest) {

        log.info("GET /executor/customers/{}/campaigns/recommendations - productId: {}",
                customerId, productId);

        CampaignRecommendationResponse response =
                recommendationService.recommendCampaigns(customerId, productId);

        log.info("캠페인 추천 완료 - 추천 개수: {}", response.getRecommendations().size());

        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }
}