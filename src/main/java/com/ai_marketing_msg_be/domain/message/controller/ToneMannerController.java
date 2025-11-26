package com.ai_marketing_msg_be.domain.message.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.message.dto.GetToneMannerListResponse;
import com.ai_marketing_msg_be.domain.message.service.ToneMannerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/executor")
@RequiredArgsConstructor
public class ToneMannerController {

    private final ToneMannerService toneMannerService;

    @PostMapping("/tone-manner")
    public ApiResponse<GetToneMannerListResponse> getToneMannerList(HttpServletRequest httpRequest) {
        log.info("Tone manner list request");

        GetToneMannerListResponse response = toneMannerService.getToneMannerList();

        log.info("Tone manner list retrieved: {} tones", response.getToneManners().size());

        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }
}