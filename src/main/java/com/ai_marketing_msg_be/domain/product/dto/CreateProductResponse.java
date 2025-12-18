package com.ai_marketing_msg_be.domain.product.dto;

import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.product.entity.StockStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 생성 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductResponse {
    private Long productId;
    private String name;
    private String category;
    private StockStatus stockStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    public static CreateProductResponse from(Product product) {
        return CreateProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .category(product.getCategory())
                .stockStatus(product.getStockStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
