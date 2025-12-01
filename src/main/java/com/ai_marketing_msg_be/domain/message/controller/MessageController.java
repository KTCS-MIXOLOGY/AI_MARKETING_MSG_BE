package com.ai_marketing_msg_be.domain.message.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.message.dto.GetMessageListResponse;
import com.ai_marketing_msg_be.domain.message.dto.MessageDetailResponse;
import com.ai_marketing_msg_be.domain.message.dto.SaveMessageRequest;
import com.ai_marketing_msg_be.domain.message.dto.SaveMessageResponse;
import com.ai_marketing_msg_be.domain.message.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/executor/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/save")
    public ApiResponse<SaveMessageResponse> saveMessage(
            @Valid @RequestBody SaveMessageRequest request,
            HttpServletRequest httpRequest) {
        log.info("Save message request: type={}",
                request.getMessageType());

        SaveMessageResponse response = messageService.saveMessage(request);

        log.info("Message saved successfully: messageId={}", response.getMessageId());

        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @GetMapping
    public ApiResponse<GetMessageListResponse> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        log.info("Get messages request: page={}, size={}", page, size);

        if (size > 100) {
            size = 100;
        }

        GetMessageListResponse response = messageService.getMessages(page, size);

        log.info("Messages retrieved: totalCount={}, currentPage={}",
                response.getTotalCount(), response.getCurrentPage());

        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @GetMapping("/{messageId}")
    public ApiResponse<MessageDetailResponse> getMessageDetail(
            @PathVariable Long messageId,
            HttpServletRequest httpRequest) {
        log.info("Get message detail request: messageId={}", messageId);

        MessageDetailResponse response = messageService.getMessageDetail(messageId);

        log.info("Message detail retrieved: messageId={}, type={}",
                messageId, response.getMessageType());

        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }
}