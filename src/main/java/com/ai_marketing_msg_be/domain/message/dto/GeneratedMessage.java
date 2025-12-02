package com.ai_marketing_msg_be.domain.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedMessage {
    private String messageId;      // UUID 임시 생성
    private Integer version;
    private String content;
    private Integer characterCount;

    public static GeneratedMessage of(Integer version, String content) {
        int charCount = content.length();

        return GeneratedMessage.builder()
                .messageId(java.util.UUID.randomUUID().toString())
                .version(version)
                .content(content)
                .characterCount(charCount)
                .build();
    }

}