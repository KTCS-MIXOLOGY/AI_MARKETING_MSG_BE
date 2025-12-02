package com.ai_marketing_msg_be.infra.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIChoice {
    private Integer index;
    private OpenAIMessage message;

    @JsonProperty("finish_reason")
    private String finishReason;
}