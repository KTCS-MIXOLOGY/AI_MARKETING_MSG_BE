package com.ai_marketing_msg_be.infra.openai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<OpenAIChoice> choices;
    private OpenAIUsage usage;
}