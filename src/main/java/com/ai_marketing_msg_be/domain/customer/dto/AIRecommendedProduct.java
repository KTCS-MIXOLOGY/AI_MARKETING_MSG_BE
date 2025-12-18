package com.ai_marketing_msg_be.domain.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 추천 상품 (내부 파싱용)")
public class AIRecommendedProduct {

    @Schema(description = "추천 순위", example = "1")
    @JsonProperty("rank")
    private Integer rank;

    @Schema(description = "상품 ID", example = "123")
    @JsonProperty("productId")
    private Long productId;

    @Schema(description = "추천 이유", example = "현재 LTE 사용 중인 VIP 고객으로...")
    @JsonProperty("reason")
    private String reason;

    @Schema(description = "기대 효과", example = "월 데이터 사용량 대비 무제한 혜택...")
    @JsonProperty("expectedBenefit")
    private String expectedBenefit;

    @Schema(description = "적합도 점수", example = "95")
    @JsonProperty("relevanceScore")
    private Integer relevanceScore;
}