package com.ai_marketing_msg_be.infra.openai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "openai.api")
public class OpenAIProperties {
    private String key;
    private String url;
    private String model;
    private Integer timeout;
    private Integer maxTokens;
    private Double temperature;
}