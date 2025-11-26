package com.ai_marketing_msg_be.domain.message.dto;


import com.ai_marketing_msg_be.domain.message.entity.Message;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveMessageResponse {

    private Long messageId;
    private String messageGroupId;
    private String messageContent;
    private Integer messageVersion;
    private Integer characterCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime savedAt;

    public static SaveMessageResponse from(Message message) {
        return SaveMessageResponse.builder()
                .messageId(message.getMessageId())
                .messageGroupId(message.getMessageGroupId())
                .messageContent(message.getMessageContent())
                .messageVersion(message.getMessageVersion())
                .characterCount(message.getCharacterCount())
                .savedAt(message.getCreatedAt())
                .build();
    }
}