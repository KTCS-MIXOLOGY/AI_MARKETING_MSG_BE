package com.ai_marketing_msg_be.domain.customer.dto;

import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "추천 상품 정보")
public class RecommendedProduct {

    @Schema(description = "추천 순위", example = "1")
    private Integer rank;

    @Schema(description = "상품 ID", example = "123")
    private Long productId;

    @Schema(description = "상품명", example = "5G 프리미엄 요금제")
    private String productName;

    @Schema(description = "카테고리", example = "요금제")
    private String category;

    @Schema(description = "가격 (원)", example = "75000")
    private BigDecimal price;

    @Schema(description = "할인율 (%)", example = "20")
    private BigDecimal discountRate;

    @Schema(description = "할인 후 가격 (원)", example = "60000")
    private BigDecimal discountedPrice;

    @Schema(description = "주요 혜택", example = "월 100GB 데이터 무료 제공, 5G 고속 인터넷")
    private String benefits;

    @Schema(description = "추천 이유 (고객 맞춤형)",
            example = "현재 LTE 요금제 사용 중인 VIP 고객으로, 5G로 업그레이드 시 더 빠른 속도와 데이터 혜택을 받을 수 있습니다.")
    private String reason;

    @Schema(description = "기대 효과",
            example = "월 평균 45GB 사용 고객에게 100GB 제공으로 추가 요금 걱정 없이 이용 가능합니다.")
    private String expectedBenefit;

    @Schema(description = "적합도 점수 (85-100)", example = "95")
    private Integer relevanceScore;

    public static RecommendedProduct fromProduct(
            Product product,
            Integer rank,
            String reason,
            String expectedBenefit,
            Integer relevanceScore
    ) {
        BigDecimal price = product.getPrice();
        BigDecimal discountRate = product.getDiscountRate();
        BigDecimal discountedPrice = null;

        if (price != null && discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
            discountedPrice = product.getDiscountedPrice();
        }

        return RecommendedProduct.builder()
                .rank(rank)
                .productId(product.getProductId())
                .productName(product.getName())
                .category(product.getCategory())
                .price(price)
                .discountRate(discountRate)
                .discountedPrice(discountedPrice)
                .benefits(product.getBenefits())
                .reason(reason)
                .expectedBenefit(expectedBenefit)
                .relevanceScore(relevanceScore)
                .build();
    }
}