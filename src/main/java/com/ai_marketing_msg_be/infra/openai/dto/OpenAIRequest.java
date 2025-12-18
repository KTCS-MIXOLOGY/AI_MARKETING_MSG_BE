package com.ai_marketing_msg_be.infra.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequest {
    private String model;
    private List<OpenAIMessage> messages;
    private Double temperature;

    @JsonProperty("max_tokens")
    @Builder.Default
    private Integer maxTokens = 1500;
}