package com.ai_marketing_msg_be.infra.openai.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     OpenAIProperties openAIProperties) {
        return builder
                .requestFactory(() -> clientHttpRequestFactory(openAIProperties))
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(OpenAIProperties openAIProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(openAIProperties.getTimeout());
        factory.setReadTimeout(openAIProperties.getTimeout());
        return factory;
    }
}