package com.ai_marketing_msg_be.domain.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "고객 맞춤 캠페인 추천 응답")
public class CampaignRecommendationResponse {

    @Schema(description = "고객 ID", example = "1")
    private Long customerId;

    @Schema(description = "고객명", example = "김철수")
    private String customerName;

    @Schema(description = "고객 프로필 요약")
    private CustomerProfileSummary customerProfile;

    @Schema(description = "타겟 상품 (productId가 지정된 경우만)")
    private TargetProductInfo targetProduct;

    @Schema(description = "추천 캠페인 목록 (최대 3개)")
    private List<RecommendedCampaign> recommendations;

    @Schema(description = "추천 생성 일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerProfileSummary {
        @Schema(description = "나이", example = "28")
        private Integer age;

        @Schema(description = "성별", example = "MALE")
        private String gender;

        @Schema(description = "멤버십 등급", example = "GOLD")
        private String membershipLevel;

        @Schema(description = "현재 요금제", example = "5G 슬림")
        private String currentPlan;

        @Schema(description = "평균 데이터 사용량", example = "35.5")
        private String avgDataUsage;

        @Schema(description = "약정 만료일", example = "2025-03-15")
        private String contractEndDate;

        @Schema(description = "최근 구매 후 경과 일수", example = "45")
        private Integer daysSinceLastPurchase;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetProductInfo {
        @Schema(description = "상품 ID", example = "150")
        private Long productId;

        @Schema(description = "상품명", example = "5G 프리미엄 요금제")
        private String productName;

        @Schema(description = "카테고리", example = "모바일")
        private String category;

        @Schema(description = "가격", example = "55000")
        private Integer price;
    }
}