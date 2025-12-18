package com.ai_marketing_msg_be.domain.message.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메시지 생성 응답")
public class GenerateMessageResponse {

    @Schema(description = "메시지 그룹 ID", example = "MSG_GROUP_001")
    private String messageGroupId;

    @Schema(description = "생성된 메시지 목록 (3개)")
    private List<GeneratedMessage> messages;

    @Schema(description = "생성 일시", example = "2024-12-03T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    @Schema(description = "타겟 고객 수", example = "15234")
    private Integer targetCustomerCount;

    public static GenerateMessageResponse of(
            String messageGroupId,
            List<GeneratedMessage> messages,
            Integer targetCustomerCount) {

        return GenerateMessageResponse.builder()
                .messageGroupId(messageGroupId)
                .messages(messages)
                .generatedAt(LocalDateTime.now())
                .targetCustomerCount(targetCustomerCount)
                .build();
    }
}