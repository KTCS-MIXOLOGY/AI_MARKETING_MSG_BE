package com.ai_marketing_msg_be.domain.message.vo;

import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.customer.dto.SegmentFilterRequest;
import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.ai_marketing_msg_be.domain.message.entity.ToneManner;
import com.ai_marketing_msg_be.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptContext {

    private SegmentFilterRequest segmentFilter;
    private Integer targetCustomerCount;

    private Customer customer;

    private Campaign campaign;
    private Product product;
    private ToneManner toneManner;
    private String additionalContext;

    public boolean isSegmentContext() {
        return segmentFilter != null;
    }

    public boolean isIndividualContext() {
        return customer != null;
    }
}