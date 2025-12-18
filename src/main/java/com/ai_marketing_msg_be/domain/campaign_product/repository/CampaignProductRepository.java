package com.ai_marketing_msg_be.domain.campaign_product.repository;

import com.ai_marketing_msg_be.domain.campaign_product.entity.CampaignProduct;
import com.ai_marketing_msg_be.domain.campaign_product.entity.CampaignProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CampaignProduct Repository
 */
@Repository
public interface CampaignProductRepository extends JpaRepository<CampaignProduct, CampaignProductId> {

    /**
     * 특정 캠페인의 모든 상품 조회
     */
    @Query("SELECT cp FROM CampaignProduct cp " +
           "JOIN FETCH cp.campaign " +
           "JOIN FETCH cp.product " +
           "WHERE cp.campaign.campaignId = :campaignId")
    List<CampaignProduct> findByCampaignId(@Param("campaignId") Long campaignId);

    /**
     * 특정 상품이 포함된 모든 캠페인 조회
     */
    @Query("SELECT cp FROM CampaignProduct cp " +
           "JOIN FETCH cp.campaign " +
           "JOIN FETCH cp.product " +
           "WHERE cp.product.productId = :productId")
    List<CampaignProduct> findByProductId(@Param("productId") Long productId);

    /**
     * 특정 캠페인에 특정 상품이 이미 매핑되어 있는지 확인
     */
    boolean existsByCampaignCampaignIdAndProductProductId(Long campaignId, Long productId);

    /**
     * 특정 캠페인의 상품 개수 조회
     */
    @Query("SELECT COUNT(cp) FROM CampaignProduct cp WHERE cp.campaign.campaignId = :campaignId")
    long countByCampaignId(@Param("campaignId") Long campaignId);

    /**
     * 특정 상품이 포함된 캠페인 개수 조회
     */
    @Query("SELECT COUNT(cp) FROM CampaignProduct cp WHERE cp.product.productId = :productId")
    long countByProductId(@Param("productId") Long productId);
}
