package com.ai_marketing_msg_be.domain.product.dto;

import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.product.entity.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 생성 요청")
public class CreateProductRequest {

    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 100, message = "상품명은 100자 이하여야 합니다")
    @Schema(description = "상품명", example = "기가 인터넷 500M")
    private String name;

    @Size(max = 50, message = "카테고리는 50자 이하여야 합니다")
    @Schema(description = "카테고리", example = "인터넷")
    private String category;

    @DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다")
    @Schema(description = "가격", example = "33000")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "할인율은 0 이상이어야 합니다")
    @DecimalMax(value = "100.0", message = "할인율은 100 이하여야 합니다")
    @Schema(description = "할인율", example = "30.0")
    private BigDecimal discountRate;

    @Schema(description = "혜택 내용", example = "3년 약정 시 월 1.1만 원 할인")
    private String benefits;

    @Schema(description = "재고 상태", example = "IN_STOCK")
    private StockStatus stockStatus;

    /**
     * DTO를 Entity로 변환
     */
    public Product toEntity() {
        return Product.builder()
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .discountRate(this.discountRate)
                .benefits(this.benefits)
                .stockStatus(this.stockStatus)
                .build();
    }
}
