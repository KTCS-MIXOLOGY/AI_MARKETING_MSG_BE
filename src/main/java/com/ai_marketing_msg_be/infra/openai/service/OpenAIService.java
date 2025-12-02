package com.ai_marketing_msg_be.infra.openai.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.infra.openai.config.OpenAIProperties;
import com.ai_marketing_msg_be.infra.openai.dto.OpenAIRequest;
import com.ai_marketing_msg_be.infra.openai.dto.OpenAIResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final OpenAIProperties openAIProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * OpenAI Chat Completion API 호출
     */
    public OpenAIResponse callChatCompletion(OpenAIRequest request) {
        try {
            log.info("OpenAI API 호출 시작 - model: {}", request.getModel());
            log.debug("Request messages: {}", request.getMessages());

            HttpHeaders headers = createHeaders();
            HttpEntity<OpenAIRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<OpenAIResponse> response = restTemplate.exchange(
                    openAIProperties.getUrl(),
                    HttpMethod.POST,
                    entity,
                    OpenAIResponse.class
            );

            OpenAIResponse responseBody = response.getBody();
            if (responseBody == null || responseBody.getChoices().isEmpty()) {
                throw new BusinessException(ErrorCode.MESSAGE_GENERATION_FAILED);
            }

            log.info("OpenAI API 호출 성공 - tokens: {}",
                    responseBody.getUsage().getTotalTokens());
            log.debug("Response: {}", responseBody.getChoices().get(0).getMessage().getContent());

            return responseBody;

        } catch (RestClientException e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OPENAI_API_CALL_FAILED);
        }
    }

    /**
     * JSON 응답 파싱
     */
    public <T> T parseJsonResponse(String content, Class<T> clazz) {
        try {
            // GPT가 ```json ... ``` 형식으로 응답할 수 있으므로 제거
            String cleanedContent = content
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            return objectMapper.readValue(cleanedContent, clazz);

        } catch (Exception e) {
            log.error("JSON 파싱 실패 - content: {}", content, e);
            throw new BusinessException(ErrorCode.INVALID_JSON_RESPONSE);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIProperties.getKey());
        return headers;
    }
}