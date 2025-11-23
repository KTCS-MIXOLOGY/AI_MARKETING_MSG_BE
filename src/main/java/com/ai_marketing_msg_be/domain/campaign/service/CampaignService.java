package com.ai_marketing_msg_be.domain.campaign.service;

import com.ai_marketing_msg_be.common.dto.PageResponse;
import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.campaign.dto.*;
import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignService {

    private final CampaignRepository campaignRepository;

    /**
     * 캠페인 목록 조회 (페이징)
     */
    public PageResponse<CampaignDto> getCampaignList(Pageable pageable) {
        log.debug("Fetching campaign list with pageable: {}", pageable);

        Page<Campaign> campaigns = campaignRepository.findAll(pageable);
        Page<CampaignDto> campaignDtos = campaigns.map(CampaignDto::from);

        return PageResponse.from(campaignDtos);
    }

    /**
     * 캠페인 상세 조회
     */
    public CampaignDto getCampaignDetail(Long campaignId) {
        log.debug("Fetching campaign detail for campaignId: {}", campaignId);

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        return CampaignDto.from(campaign);
    }

    /**
     * 캠페인 생성
     */
    @Transactional
    public CreateCampaignResponse createCampaign(CreateCampaignRequest request, Long userId) {
        log.debug("Creating campaign with request: {}, userId: {}", request, userId);

        // 중복 캠페인명 검증 (선택사항)
        if (campaignRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new BusinessException(ErrorCode.CAMPAIGN_ALREADY_EXISTS,
                    "동일한 이름의 캠페인이 이미 존재합니다.");
        }

        // 엔티티 생성
        Campaign campaign = request.toEntity(userId);

        // 날짜 검증
        campaign.validateDateRange();

        // 저장
        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("Campaign created successfully. campaignId: {}", savedCampaign.getCampaignId());

        return CreateCampaignResponse.from(savedCampaign);
    }

    /**
     * 캠페인 수정
     */
    @Transactional
    public UpdateCampaignResponse updateCampaign(Long campaignId, UpdateCampaignRequest request, Long userId) {
        log.debug("Updating campaign. campaignId: {}, request: {}, userId: {}", campaignId, request, userId);

        // 캠페인 조회
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        // 권한 검증 (선택사항 - 본인이 생성한 캠페인만 수정 가능)
        if (!campaign.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 캠페인을 수정할 권한이 없습니다.");
        }

        // 업데이트
        campaign.update(
                request.getName(),
                request.getType(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus()
        );

        // 날짜 검증
        campaign.validateDateRange();

        log.info("Campaign updated successfully. campaignId: {}", campaignId);

        return UpdateCampaignResponse.from(campaign, userId);
    }

    /**
     * 캠페인 삭제
     */
    @Transactional
    public DeleteCampaignResponse deleteCampaign(Long campaignId, Long userId) {
        log.debug("Deleting campaign. campaignId: {}, userId: {}", campaignId, userId);

        // 캠페인 조회
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        // 권한 검증
        if (!campaign.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 캠페인을 삭제할 권한이 없습니다.");
        }

        // 삭제 가능 여부 확인
        if (!campaign.canBeDeleted()) {
            throw new BusinessException(ErrorCode.CAMPAIGN_CANNOT_BE_DELETED,
                    "ACTIVE 또는 COMPLETED 상태의 캠페인은 삭제할 수 없습니다.");
        }

        // 삭제
        campaignRepository.delete(campaign);
        log.info("Campaign deleted successfully. campaignId: {}", campaignId);

        return DeleteCampaignResponse.of(campaignId, userId);
    }
}
