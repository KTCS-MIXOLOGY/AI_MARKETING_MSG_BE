package com.ai_marketing_msg_be.domain.messagelog.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.common.dto.PageResponse;
import com.ai_marketing_msg_be.domain.messagelog.dto.MessageLogDetailResponse;
import com.ai_marketing_msg_be.domain.messagelog.dto.MessageLogListResponse;
import com.ai_marketing_msg_be.domain.messagelog.service.MessageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/messages")
@RequiredArgsConstructor
@Tag(name = "MessageLog", description = "메시지 로그 API")
public class MessageLogController {

    private final MessageLogService messageLogService;

    /**
     * 메시지 로그 목록 조회
     */
    @GetMapping
    @Operation(summary = "메시지 로그 목록 조회", description = "모든 메시지 로그를 페이징하여 조회합니다")
    public ResponseEntity<ApiResponse<PageResponse<MessageLogListResponse>>> getMessageLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        log.info("GET /admin/messages - Fetching message logs (page: {}, size: {})", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageLogListResponse> messageLogs = messageLogService.getMessageLogs(pageable);

        PageResponse<MessageLogListResponse> pageResponse = PageResponse.from(messageLogs);

        return ResponseEntity.ok(
                ApiResponse.ok(pageResponse, request.getRequestURI())
        );
    }

    /**
     * 메시지 로그 상세 조회
     */
    @GetMapping("/{messageId}")
    @Operation(summary = "메시지 로그 상세 조회", description = "특정 메시지 로그의 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<MessageLogDetailResponse>> getMessageLogDetail(
            @PathVariable Long messageId,
            HttpServletRequest request
    ) {
        log.info("GET /admin/messages/{} - Fetching message log detail", messageId);

        MessageLogDetailResponse messageLog = messageLogService.getMessageLogDetail(messageId);

        return ResponseEntity.ok(
                ApiResponse.ok(messageLog, request.getRequestURI())
        );
    }

    /**
     * 캠페인별 메시지 로그 조회
     */
    @GetMapping("/campaign/{campaignId}")
    @Operation(summary = "캠페인별 메시지 로그 조회", description = "특정 캠페인의 메시지 로그를 조회합니다")
    public ResponseEntity<ApiResponse<PageResponse<MessageLogListResponse>>> getMessageLogsByCampaign(
            @PathVariable Long campaignId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        log.info("GET /admin/messages/campaign/{} - Fetching message logs for campaign", campaignId);

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageLogListResponse> messageLogs = messageLogService.getMessageLogsByCampaign(campaignId, pageable);

        PageResponse<MessageLogListResponse> pageResponse = PageResponse.from(messageLogs);

        return ResponseEntity.ok(
                ApiResponse.ok(pageResponse, request.getRequestURI())
        );
    }

    /**
     * 사용자별 메시지 로그 조회
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 메시지 로그 조회", description = "특정 사용자가 생성한 메시지 로그를 조회합니다")
    public ResponseEntity<ApiResponse<PageResponse<MessageLogListResponse>>> getMessageLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        log.info("GET /admin/messages/user/{} - Fetching message logs for user", userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageLogListResponse> messageLogs = messageLogService.getMessageLogsByUser(userId, pageable);

        PageResponse<MessageLogListResponse> pageResponse = PageResponse.from(messageLogs);

        return ResponseEntity.ok(
                ApiResponse.ok(pageResponse, request.getRequestURI())
        );
    }
}
