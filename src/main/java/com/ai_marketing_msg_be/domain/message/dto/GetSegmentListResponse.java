package com.ai_marketing_msg_be.domain.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 세그먼트 목록 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "고객 세그먼트 목록 조회 응답")
public class GetSegmentListResponse {

    @Schema(description = "세그먼트 목록")
    private List<SegmentDto> segments;

    @Schema(description = "총 세그먼트 개수", example = "5")
    private Integer totalCount;
}
