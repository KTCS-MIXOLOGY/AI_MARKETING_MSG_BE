package com.ai_marketing_msg_be.domain.campaign_product.entity;

import com.ai_marketing_msg_be.common.entity.BaseEntity;
import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캠페인-상품 매핑 엔티티
 */
@Entity
@Table(name = "캠페인_상품_매핑")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignProduct extends BaseEntity {

    @EmbeddedId
    private CampaignProductId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("campaignId")
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder
    public CampaignProduct(Campaign campaign, Product product) {
        this.id = new CampaignProductId(campaign.getCampaignId(), product.getProductId());
        this.campaign = campaign;
        this.product = product;
    }

    /**
     * 정적 팩토리 메서드
     */
    public static CampaignProduct of(Campaign campaign, Product product) {
        return CampaignProduct.builder()
                .campaign(campaign)
                .product(product)
                .build();
    }
}
