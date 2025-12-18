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
@Schema(description = "고객 맞춤 상품 추천 응답")
public class ProductRecommendationResponse {
    @Schema(description = "고객 ID", example = "1")
    private Long customerId;

    @Schema(description = "고객명", example = "김철수")
    private String customerName;

    @Schema(description = "고객 프로필 요약")
    private CustomerProfileSummary customerProfile;

    @Schema(description = "타겟 캠페인 (productId가 지정된 경우만)")
    private TargetCampaignInfo targetCampaign;

    @Schema(description = "추천 상품 목록 (최대 3개)")
    private List<RecommendedProduct> recommendations;

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

        @Schema(description = "가입일", example = "2012-02-14")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private String joinDate;

        @Schema(description = "이용 기간 (년)", example = "13")
        private Integer yearsAsCustomer;

        @Schema(description = "거주 지역", example = "광주")
        private String region;

        @Schema(description = "현재 요금제", example = "5G 슬림")
        private String currentPlan;

        @Schema(description = "현재 기기", example = "갤럭시 S23")
        private String currentDevice;

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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "타겟 캠페인 정보")
    public static class TargetCampaignInfo {

        @Schema(description = "캠페인 ID", example = "1")
        private Long campaignId;

        @Schema(description = "캠페인명", example = "5G 프리미엄 전환 프로모션")
        private String campaignName;

        @Schema(description = "캠페인 유형", example = "업셀링")
        private String campaignType;

        @Schema(description = "캠페인 설명", example = "LTE에서 5G 프리미엄 요금제로 전환 시 특별 혜택 제공")
        private String description;
    }
}