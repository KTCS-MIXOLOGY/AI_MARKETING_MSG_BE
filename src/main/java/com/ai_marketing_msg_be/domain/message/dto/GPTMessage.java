package com.ai_marketing_msg_be.domain.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GPTMessage {
    private Integer version;
    private String content;
}