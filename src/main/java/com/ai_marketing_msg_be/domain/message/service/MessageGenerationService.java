package com.ai_marketing_msg_be.domain.message.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.repository.CampaignRepository;
import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.ai_marketing_msg_be.domain.customer.repository.CustomerRepository;
import com.ai_marketing_msg_be.domain.customer.service.CustomerService;
import com.ai_marketing_msg_be.domain.message.dto.GPTMessage;
import com.ai_marketing_msg_be.domain.message.dto.GenerateIndividualMessageRequest;
import com.ai_marketing_msg_be.domain.message.dto.GenerateMessageResponse;
import com.ai_marketing_msg_be.domain.message.dto.GenerateSegmentMessageRequest;
import com.ai_marketing_msg_be.domain.message.dto.GeneratedMessage;
import com.ai_marketing_msg_be.domain.message.entity.ToneManner;
import com.ai_marketing_msg_be.domain.message.vo.PromptContext;
import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.product.repository.ProductRepository;
import com.ai_marketing_msg_be.infra.openai.config.OpenAIProperties;
import com.ai_marketing_msg_be.infra.openai.dto.OpenAIMessage;
import com.ai_marketing_msg_be.infra.openai.dto.OpenAIRequest;
import com.ai_marketing_msg_be.infra.openai.dto.OpenAIResponse;
import com.ai_marketing_msg_be.infra.openai.service.OpenAIService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageGenerationService {

    private final OpenAIService openAIService;
    private final PromptTemplateEngine promptTemplateEngine;
    private final OpenAIProperties openAIProperties;
    private final ObjectMapper objectMapper;

    private final CampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    public GenerateMessageResponse generateSegmentMessage(GenerateSegmentMessageRequest request) {
        log.info("세그먼트 메시지 생성 요청 - campaignId: {}, productId: {}",
                request.getCampaignId(), request.getProductId());

        Campaign campaign = findCampaignById(request.getCampaignId());
        Product product = findProductById(request.getProductId());
        ToneManner toneManner = findToneMannerById(request.getToneId());

        int targetCustomerCount = customerService.countBySegmentFilter(request.getSegmentFilter());
        log.info("타겟 고객 수: {}", targetCustomerCount);

        PromptContext context = PromptContext.builder()
                .segmentFilter(request.getSegmentFilter())
                .targetCustomerCount(targetCustomerCount)
                .campaign(campaign)
                .product(product)
                .toneManner(toneManner)
                .additionalContext(request.getAdditionalContext())
                .build();

        List<GeneratedMessage> messages = generateMessages(context);

        String messageGroupId = generateMessageGroupId();

        log.info("세그먼트 메시지 생성 완료 - messageGroupId: {}, 생성된 메시지 수: {}",
                messageGroupId, messages.size());

        return GenerateMessageResponse.of(messageGroupId, messages, targetCustomerCount);
    }

    public GenerateMessageResponse generateIndividualMessage(GenerateIndividualMessageRequest request) {
        log.info("개별 고객 메시지 생성 요청 - customerId: {}, campaignId: {}, productId: {}",
                request.getCustomerId(), request.getCampaignId(), request.getProductId());

        Customer customer = findCustomerById(request.getCustomerId());
        Campaign campaign = findCampaignById(request.getCampaignId());
        Product product = findProductById(request.getProductId());
        ToneManner toneManner = findToneMannerById(request.getToneId());

        PromptContext context = PromptContext.builder()
                .customer(customer)
                .campaign(campaign)
                .product(product)
                .toneManner(toneManner)
                .additionalContext(request.getAdditionalContext())
                .build();

        List<GeneratedMessage> messages = generateMessages(context);

        String messageGroupId = generateMessageGroupId();

        log.info("개별 고객 메시지 생성 완료 - messageGroupId: {}, 생성된 메시지 수: {}",
                messageGroupId, messages.size());

        return GenerateMessageResponse.of(messageGroupId, messages, 1);
    }

    private List<GeneratedMessage> generateMessages(PromptContext context) {
        try {
            String prompt = promptTemplateEngine.buildPrompt(context);
            log.debug("Generated prompt: {}", prompt);

            OpenAIRequest openAIRequest = OpenAIRequest.builder()
                    .model(openAIProperties.getModel())
                    .messages(List.of(
                            OpenAIMessage.builder()
                                    .role("system")
                                    .content("당신은 KT의 전문 마케팅 메시지 작성자입니다.")
                                    .build(),
                            OpenAIMessage.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .temperature(openAIProperties.getTemperature())
                    .maxTokens(openAIProperties.getMaxTokens())
                    .build();

            OpenAIResponse response = openAIService.callChatCompletion(openAIRequest);

            String content = response.getChoices().get(0).getMessage().getContent();
            log.debug("GPT response content: {}", content);

            List<GPTMessage> gptMessages = parseGPTResponse(content);

            return gptMessages.stream()
                    .map(gpt -> GeneratedMessage.of(gpt.getVersion(), gpt.getContent()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("메시지 생성 실패", e);
            throw new BusinessException(ErrorCode.MESSAGE_GENERATION_FAILED);
        }
    }


    private List<GPTMessage> parseGPTResponse(String content) {
        try {
            String cleanedContent = content
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            return objectMapper.readValue(
                    cleanedContent,
                    new TypeReference<List<GPTMessage>>() {
                    }
            );

        } catch (Exception e) {
            log.error("GPT 응답 파싱 실패 - content: {}", content, e);
            throw new BusinessException(ErrorCode.INVALID_JSON_RESPONSE);
        }
    }

    private String generateMessageGroupId() {
        return "MSG_GROUP_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private Campaign findCampaignById(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    private ToneManner findToneMannerById(String toneId) {
        if (toneId == null || toneId.isEmpty()) {
            return ToneManner.FRIENDLY;
        }
        return ToneManner.fromToneId(toneId);
    }
}