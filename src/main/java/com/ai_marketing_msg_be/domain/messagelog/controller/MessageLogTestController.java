package com.ai_marketing_msg_be.domain.messagelog.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.messagelog.entity.MessageLog;
import com.ai_marketing_msg_be.domain.messagelog.entity.MessageType;
import com.ai_marketing_msg_be.domain.messagelog.entity.TargetType;
import com.ai_marketing_msg_be.domain.messagelog.repository.MessageLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/messages")
@RequiredArgsConstructor
public class MessageLogTestController {

    private final MessageLogRepository messageLogRepository;

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<String>> seedTestData(HttpServletRequest request) {
        // 테스트 데이터 1
        MessageLog message1 = MessageLog.builder()
                .campaignId(1L)
                .segmentId(1L)
                .messageContent("20대 여성 VIP 고객님을 위한 5G 전환 프로모션 안내입니다. 기존 요금제 대비 20% 할인 혜택과 함께 최신 5G 스마트폰을 특별 가격으로 제공해 드립니다.")
                .messageVersion(1)
                .messageType(MessageType.SMS)
                .characterCount(78)
                .aiModelUsed("gpt-4")
                .generationPrompt("20대 여성 VIP 고객 대상 5G 전환 프로모션 메시지 생성")
                .tone("friendly")
                .createdBy(3L)
                .targetType(TargetType.SEGMENT)
                .segmentName("20대 여성 VIP")
                .build();

        // 테스트 데이터 2
        MessageLog message2 = MessageLog.builder()
                .campaignId(1L)
                .segmentId(2L)
                .messageContent("프리미엄 고객님께 특별한 혜택을 준비했습니다. 최신 5G 요금제로 변경하시면 3개월간 월 10,000원 할인과 데이터 무제한 서비스를 제공해 드립니다.")
                .messageVersion(1)
                .messageType(MessageType.LMS)
                .characterCount(92)
                .aiModelUsed("gpt-4")
                .generationPrompt("프리미엄 고객 대상 5G 전환 혜택 안내")
                .tone("professional")
                .createdBy(3L)
                .targetType(TargetType.SEGMENT)
                .segmentName("프리미엄 고객")
                .build();

        // 테스트 데이터 3
        MessageLog message3 = MessageLog.builder()
                .campaignId(1L)
                .segmentId(1L)
                .messageContent("안녕하세요! 고객님의 현재 요금제를 분석한 결과, 5G 요금제로 변경하시면 월 평균 15,000원을 절약하실 수 있습니다. 지금 바로 상담 신청하세요!")
                .messageVersion(2)
                .messageType(MessageType.SMS)
                .characterCount(85)
                .aiModelUsed("gpt-4-turbo")
                .generationPrompt("요금제 절감 효과 강조 메시지")
                .tone("conversational")
                .createdBy(3L)
                .targetType(TargetType.SEGMENT)
                .segmentName("20대 여성 VIP")
                .build();

        messageLogRepository.save(message1);
        messageLogRepository.save(message2);
        messageLogRepository.save(message3);

        return ResponseEntity.ok(
                ApiResponse.ok("3 test messages created successfully", request.getRequestURI())
        );
    }
}
