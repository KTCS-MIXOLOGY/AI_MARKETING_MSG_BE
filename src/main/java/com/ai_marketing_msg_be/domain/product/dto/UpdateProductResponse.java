package com.ai_marketing_msg_be.domain.product.dto;

import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.product.entity.StockStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 수정 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductResponse {
    private Long productId;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal discountRate;
    private String benefits;
    private String conditions;
    private StockStatus stockStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    public static UpdateProductResponse from(Product product) {
        return UpdateProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .discountRate(product.getDiscountRate())
                .benefits(product.getBenefits())
                .conditions(product.getConditions())
                .stockStatus(product.getStockStatus())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
