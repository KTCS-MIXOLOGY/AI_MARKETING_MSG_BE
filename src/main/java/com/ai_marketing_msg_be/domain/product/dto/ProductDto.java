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
 * 상품 목록 조회용 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long productId;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal discountRate;
    private StockStatus stockStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static ProductDto from(Product product) {
        return ProductDto.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .discountRate(product.getDiscountRate())
                .stockStatus(product.getStockStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
