package com.ai_marketing_msg_be.domain.message.dto;

import com.ai_marketing_msg_be.domain.message.entity.Segment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 세그먼트 상세 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "고객 세그먼트 상세 조회 응답")
public class GetSegmentDetailResponse {

    @Schema(description = "세그먼트 ID", example = "1")
    private Long segmentId;

    @Schema(description = "세그먼트명", example = "30대 서울 VIP 고객")
    private String segmentName;

    @Schema(description = "최소 연령", example = "30")
    private Integer ageMin;

    @Schema(description = "최대 연령", example = "39")
    private Integer ageMax;

    @Schema(description = "성별", example = "MALE")
    private String gender;

    @Schema(description = "지역 목록", example = "[\"SEOUL\", \"BUSAN\"]")
    private List<String> regions;

    @Schema(description = "멤버십 등급", example = "VIP")
    private String membershipLevel;

    @Schema(description = "최대 미구매 일수", example = "90")
    private Integer recencyMaxDays;

    @Schema(description = "타겟 고객 수", example = "1500")
    private Integer targetCustomerCount;

    @Schema(description = "세그먼트 설명", example = "30대 서울 거주 VIP 고객 타겟 세그먼트")
    private String description;

    @Schema(description = "활성 필터 개수", example = "4")
    private Integer activeFilterCount;

    @Schema(description = "생성일시", example = "2024-12-01T10:30:00")
    private LocalDateTime createdAt;

    /**
     * Entity를 DetailResponse DTO로 변환
     */
    public static GetSegmentDetailResponse fromEntity(Segment entity) {
        return GetSegmentDetailResponse.builder()
                .segmentId(entity.getSegmentId())
                .segmentName(entity.getSegmentName())
                .ageMin(entity.getAgeMin())
                .ageMax(entity.getAgeMax())
                .gender(entity.getGender())
                .regions(entity.getRegions())
                .membershipLevel(entity.getMembershipLevel())
                .recencyMaxDays(entity.getRecencyMaxDays())
                .targetCustomerCount(entity.getTargetCustomerCount())
                .description(entity.getDescription())
                .activeFilterCount(entity.getActiveFilterCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
