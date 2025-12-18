package com.ai_marketing_msg_be.domain.message.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.message.dto.GetMessageLogDetailResponse;
import com.ai_marketing_msg_be.domain.message.dto.GetMessageLogListResponse;
import com.ai_marketing_msg_be.domain.message.service.AdminMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/messages")
@RequiredArgsConstructor
@Tag(name = "Admin Message", description = "관리자 메시지 조회 API")
public class AdminMessageController {

    private final AdminMessageService adminMessageService;

    @GetMapping
    @Operation(summary = "메시지 목록 조회", description = "관리자가 모든 메시지를 조회합니다")
    public ApiResponse<GetMessageLogListResponse> getMessageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {

        log.info("Admin: Get messages - page={}, size={}", page, size);
        GetMessageLogListResponse response = adminMessageService.getMessageList(page, size);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @GetMapping("/{messageId}")
    @Operation(summary = "메시지 상세 조회", description = "관리자가 특정 메시지의 상세 정보를 조회합니다")
    public ApiResponse<GetMessageLogDetailResponse> getMessageDetail(
            @PathVariable Long messageId,
            HttpServletRequest httpRequest) {

        log.info("Admin: Get message detail - messageId={}", messageId);
        GetMessageLogDetailResponse response = adminMessageService.getMessageDetail(messageId);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @GetMapping("/campaign/{campaignId}")
    @Operation(summary = "캠페인별 메시지 조회", description = "특정 캠페인의 모든 메시지를 조회합니다")
    public ApiResponse<GetMessageLogListResponse> getMessagesByCampaign(
            @PathVariable Long campaignId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {

        log.info("Admin: Get messages by campaign - campaignId={}, page={}, size={}",
                campaignId, page, size);
        GetMessageLogListResponse response = adminMessageService.getMessagesByCampaign(campaignId, page, size);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 메시지 조회", description = "특정 사용자(실행자)가 생성한 모든 메시지를 조회합니다")
    public ApiResponse<GetMessageLogListResponse> getMessagesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {

        log.info("Admin: Get messages by user - userId={}, page={}, size={}",
                userId, page, size);
        GetMessageLogListResponse response = adminMessageService.getMessagesByUser(userId, page, size);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }
}