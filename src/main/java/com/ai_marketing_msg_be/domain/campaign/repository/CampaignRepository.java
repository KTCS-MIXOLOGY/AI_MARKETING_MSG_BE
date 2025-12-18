package com.ai_marketing_msg_be.domain.campaign.repository;

import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.entity.CampaignStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // 캠페인 목록 조회 (페이징)
    Page<Campaign> findAll(Pageable pageable);

    // 사용자별 캠페인 조회
    Page<Campaign> findByUserId(Long userId, Pageable pageable);

    // 상태별 캠페인 조회
    Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);

    List<Campaign> findByStatus(CampaignStatus status);

    // 캠페인명으로 검색
    Page<Campaign> findByNameContaining(String name, Pageable pageable);

    // 특정 기간 내 캠페인 조회
    @Query("SELECT c FROM Campaign c WHERE c.startDate <= :endDate AND c.endDate >= :startDate")
    List<Campaign> findCampaignsInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 활성 캠페인 조회
    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.startDate <= :today AND c.endDate >= :today")
    List<Campaign> findActiveCampaigns(@Param("today") LocalDate today);

    // 캠페인 존재 여부 확인
    boolean existsByNameAndUserId(String name, Long userId);

    // ID와 사용자로 캠페인 조회
    Optional<Campaign> findByCampaignIdAndUserId(Long campaignId, Long userId);
}
