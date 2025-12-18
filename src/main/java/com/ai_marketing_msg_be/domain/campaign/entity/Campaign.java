package com.ai_marketing_msg_be.domain.campaign.entity;

import com.ai_marketing_msg_be.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "캠페인")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campaign extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    private CampaignType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    @Builder
    public Campaign(Long userId, String name, CampaignType type, String description,
                    LocalDate startDate, LocalDate endDate, CampaignStatus status) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status != null ? status : CampaignStatus.DRAFT;
    }

    // 비즈니스 로직
    public void update(String name, CampaignType type, String description,
                      LocalDate startDate, LocalDate endDate, CampaignStatus status) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public void updateStatus(CampaignStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return this.status == CampaignStatus.ACTIVE;
    }

    public boolean canBeDeleted() {
        return this.status == CampaignStatus.DRAFT || this.status == CampaignStatus.CANCELLED;
    }

    public void validateDateRange() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다.");
        }
    }
}
