package com.ai_marketing_msg_be.domain.product.entity;

import com.ai_marketing_msg_be.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 엔티티
 */
@Entity
@Table(name = "상품")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "stock_status", length = 20)
    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;

    @Builder
    public Product(String name, String category, BigDecimal price, BigDecimal discountRate,
                   String benefits, StockStatus stockStatus) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.discountRate = discountRate;
        this.benefits = benefits;
        this.stockStatus = stockStatus != null ? stockStatus : StockStatus.IN_STOCK;
    }

    /**
     * 상품 정보 업데이트
     */
    public void update(String name, String category, BigDecimal price, BigDecimal discountRate,
                      String benefits, StockStatus stockStatus) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.discountRate = discountRate;
        this.benefits = benefits;
        this.stockStatus = stockStatus;
    }

    /**
     * 삭제 가능 여부 확인
     * 품절 상태일 때만 삭제 가능
     */
    public boolean canBeDeleted() {
        return this.stockStatus == StockStatus.OUT_OF_STOCK;
    }

    /**
     * 가격 유효성 검증
     */
    public void validatePrice() {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        if (discountRate != null && (discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException("할인율은 0~100 사이여야 합니다.");
        }
    }

    /**
     * 할인된 최종 가격 계산
     */
    public BigDecimal getDiscountedPrice() {
        if (price == null || discountRate == null) {
            return price;
        }
        BigDecimal discountAmount = price.multiply(discountRate).divide(new BigDecimal("100"));
        return price.subtract(discountAmount);
    }
}
