package com.ai_marketing_msg_be.domain.campaign_product.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.repository.CampaignRepository;
import com.ai_marketing_msg_be.domain.campaign_product.dto.AddProductToCampaignResponse;
import com.ai_marketing_msg_be.domain.campaign_product.dto.CampaignProductDto;
import com.ai_marketing_msg_be.domain.campaign_product.dto.RemoveProductFromCampaignResponse;
import com.ai_marketing_msg_be.domain.campaign_product.entity.CampaignProduct;
import com.ai_marketing_msg_be.domain.campaign_product.entity.CampaignProductId;
import com.ai_marketing_msg_be.domain.campaign_product.repository.CampaignProductRepository;
import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CampaignProduct 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignProductService {

    private final CampaignProductRepository campaignProductRepository;
    private final CampaignRepository campaignRepository;
    private final ProductRepository productRepository;

    /**
     * 캠페인에 상품 추가
     */
    @Transactional
    public AddProductToCampaignResponse addProductToCampaign(Long campaignId, Long productId) {
        log.info("Adding product {} to campaign {}", productId, campaignId);

        // 캠페인 존재 확인
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND,
                        "캠페인을 찾을 수 없습니다. campaignId: " + campaignId));

        // 상품 존재 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                        "상품을 찾을 수 없습니다. productId: " + productId));

        // 중복 확인
        if (campaignProductRepository.existsByCampaignCampaignIdAndProductProductId(campaignId, productId)) {
            throw new BusinessException(ErrorCode.CAMPAIGN_PRODUCT_ALREADY_EXISTS,
                    "이미 캠페인에 추가된 상품입니다. campaignId: " + campaignId + ", productId: " + productId);
        }

        // 매핑 생성
        CampaignProduct campaignProduct = CampaignProduct.of(campaign, product);
        CampaignProduct saved = campaignProductRepository.save(campaignProduct);

        log.info("Product {} added to campaign {} successfully", productId, campaignId);
        return AddProductToCampaignResponse.from(saved);
    }

    /**
     * 캠페인에서 상품 제거
     */
    @Transactional
    public RemoveProductFromCampaignResponse removeProductFromCampaign(Long campaignId, Long productId) {
        log.info("Removing product {} from campaign {}", productId, campaignId);

        // 매핑 존재 확인
        CampaignProductId id = new CampaignProductId(campaignId, productId);
        CampaignProduct campaignProduct = campaignProductRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_PRODUCT_NOT_FOUND,
                        "캠페인-상품 매핑을 찾을 수 없습니다. campaignId: " + campaignId + ", productId: " + productId));

        // 삭제
        campaignProductRepository.delete(campaignProduct);

        log.info("Product {} removed from campaign {} successfully", productId, campaignId);
        return RemoveProductFromCampaignResponse.of(campaignId, productId);
    }

    /**
     * 특정 캠페인의 모든 상품 조회
     */
    public List<CampaignProductDto> getProductsByCampaignId(Long campaignId) {
        log.info("Fetching products for campaign {}", campaignId);

        // 캠페인 존재 확인
        if (!campaignRepository.existsById(campaignId)) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND,
                    "캠페인을 찾을 수 없습니다. campaignId: " + campaignId);
        }

        List<CampaignProduct> campaignProducts = campaignProductRepository.findByCampaignId(campaignId);
        return campaignProducts.stream()
                .map(CampaignProductDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 상품이 포함된 모든 캠페인 조회
     */
    public List<CampaignProductDto> getCampaignsByProductId(Long productId) {
        log.info("Fetching campaigns for product {}", productId);

        // 상품 존재 확인
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                    "상품을 찾을 수 없습니다. productId: " + productId);
        }

        List<CampaignProduct> campaignProducts = campaignProductRepository.findByProductId(productId);
        return campaignProducts.stream()
                .map(CampaignProductDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 캠페인의 상품 개수 조회
     */
    public long countProductsByCampaignId(Long campaignId) {
        log.info("Counting products for campaign {}", campaignId);
        return campaignProductRepository.countByCampaignId(campaignId);
    }

    /**
     * 특정 상품이 포함된 캠페인 개수 조회
     */
    public long countCampaignsByProductId(Long productId) {
        log.info("Counting campaigns for product {}", productId);
        return campaignProductRepository.countByProductId(productId);
    }
}
