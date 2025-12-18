package com.ai_marketing_msg_be.domain.campaign_product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * CampaignProduct 복합키 (Composite Key)
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class CampaignProductId implements Serializable {

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "product_id")
    private Long productId;

    public Long getCampaignId() {
        return campaignId;
    }

    public Long getProductId() {
        return productId;
    }
}
