package com.ai_marketing_msg_be.domain.message.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.message.dto.GenerateIndividualMessageRequest;
import com.ai_marketing_msg_be.domain.message.dto.GenerateMessageResponse;
import com.ai_marketing_msg_be.domain.message.dto.GenerateSegmentMessageRequest;
import com.ai_marketing_msg_be.domain.message.service.MessageGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/executor/messages/generate")
@RequiredArgsConstructor
@Tag(name = "Message Generation", description = "AI 메시지 생성 API")
public class MessageGenerationController {

    private final MessageGenerationService messageGenerationService;

    @PostMapping("/segment")
    @PreAuthorize("hasRole('EXECUTOR')")
    @Operation(summary = "세그먼트 메시지 생성",
            description = "고객 세그먼트 기반으로 3가지 버전의 마케팅 메시지를 자동 생성합니다.")
    public ApiResponse<GenerateMessageResponse> generateSegmentMessage(
            @Valid @RequestBody GenerateSegmentMessageRequest request,
            HttpServletRequest httpRequest) {

        log.info("POST /executor/messages/generate/segment - campaignId: {}, productId: {}",
                request.getCampaignId(), request.getProductId());

        GenerateMessageResponse response = messageGenerationService.generateSegmentMessage(request);

        log.info("세그먼트 메시지 생성 완료 - messageGroupId: {}, 메시지 수: {}",
                response.getMessageGroupId(), response.getMessages().size());

        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @PostMapping("/individual")
    @PreAuthorize("hasRole('EXECUTOR')")
    @Operation(summary = "개별 고객 메시지 생성",
            description = "특정 고객의 프로필 기반으로 3가지 버전의 개인화 메시지를 자동 생성합니다.")
    public ApiResponse<GenerateMessageResponse> generateIndividualMessage(
            @Valid @RequestBody GenerateIndividualMessageRequest request,
            HttpServletRequest httpRequest) {

        log.info("POST /executor/messages/generate/individual - customerId: {}, campaignId: {}, productId: {}",
                request.getCustomerId(), request.getCampaignId(), request.getProductId());

        GenerateMessageResponse response = messageGenerationService.generateIndividualMessage(request);

        log.info("개별 고객 메시지 생성 완료 - messageGroupId: {}, 메시지 수: {}",
                response.getMessageGroupId(), response.getMessages().size());

        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }
}