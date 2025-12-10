package com.ai_marketing_msg_be.domain.customer.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.entity.CampaignStatus;
import com.ai_marketing_msg_be.domain.campaign.repository.CampaignRepository;
import com.ai_marketing_msg_be.domain.customer.dto.AIRecommendedCampaign;
import com.ai_marketing_msg_be.domain.customer.dto.CampaignRecommendationResponse;
import com.ai_marketing_msg_be.domain.customer.dto.CampaignRecommendationResponse.CustomerProfileSummary;
import com.ai_marketing_msg_be.domain.customer.dto.CampaignRecommendationResponse.TargetProductInfo;
import com.ai_marketing_msg_be.domain.customer.dto.RecommendedCampaign;
import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.ai_marketing_msg_be.domain.customer.repository.CustomerRepository;
import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.product.repository.ProductRepository;
import com.ai_marketing_msg_be.infra.openai.config.OpenAIProperties;
import com.ai_marketing_msg_be.infra.openai.dto.OpenAIMessage;
import com.ai_marketing_msg_be.infra.openai.dto.OpenAIRequest;
import com.ai_marketing_msg_be.infra.openai.dto.OpenAIResponse;
import com.ai_marketing_msg_be.infra.openai.service.OpenAIService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerRecommendationService {

    private final CustomerRepository customerRepository;
    private final CampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final OpenAIService openAIService;
    private final OpenAIProperties openAIProperties;
    private final ObjectMapper objectMapper;

    public CampaignRecommendationResponse recommendCampaigns(Long customerId, Long productId) {
        log.info("캠페인 추천 요청 - customerId: {}, productId: {}", customerId, productId);

        Customer customer = findCustomerById(customerId);

        List<Campaign> activeCampaigns = campaignRepository.findByStatus(CampaignStatus.ACTIVE);
        log.info("활성 캠페인 수: {}", activeCampaigns.size());

        Product targetProduct = null;
        if (productId != null) {
            targetProduct = findProductById(productId);
            log.info("타겟 상품: {}", targetProduct.getName());
        }

        String prompt = buildCampaignRecommendationPrompt(customer, activeCampaigns, targetProduct);
        log.debug("생성된 프롬프트:\n{}", prompt);

        List<AIRecommendedCampaign> aiRecommendations = callOpenAIForCampaignRecommendation(prompt);

        Map<Long, Campaign> campaignMap = activeCampaigns.stream()
                .collect(Collectors.toMap(Campaign::getCampaignId, c -> c));

        List<RecommendedCampaign> recommendations = aiRecommendations.stream()
                .filter(ai -> campaignMap.containsKey(ai.getCampaignId()))
                .map(ai -> RecommendedCampaign.fromCampaign(
                        campaignMap.get(ai.getCampaignId()),
                        ai.getRank(),
                        ai.getReason(),
                        ai.getExpectedBenefit(),
                        ai.getRelevanceScore()
                ))
                .collect(Collectors.toList());

        return CampaignRecommendationResponse.builder()
                .customerId(customerId)
                .customerName(customer.getName())
                .customerProfile(buildCustomerProfileSummary(customer))
                .targetProduct(targetProduct != null ? buildTargetProductInfo(targetProduct) : null)
                .recommendations(recommendations)
                .generatedAt(LocalDateTime.now())
                .build();
    }


    private String buildCampaignRecommendationPrompt(
            Customer customer,
            List<Campaign> campaigns,
            Product targetProduct) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 KT의 개인화 마케팅 캠페인 추천 전문가입니다.\n\n");

        prompt.append("### [고객 프로필]\n");
        prompt.append(String.format("- 이름: %s\n", customer.getName()));
        prompt.append(String.format("- 나이: %d세\n", customer.getAge()));
        prompt.append(String.format("- 성별: %s\n",
                customer.getGender() != null ? customer.getGender().getDescription() : "미지정"));
        prompt.append(String.format("- 멤버십 등급: %s\n",
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "일반"));
        prompt.append(String.format("- 현재 요금제: %s\n", customer.getCurrentPlan()));

        if (customer.getAvgDataUsageGb() != null) {
            prompt.append(String.format("- 월 평균 데이터 사용량: %.1fGB\n", customer.getAvgDataUsageGb()));
        }

        if (customer.getContractEndDate() != null) {
            prompt.append(String.format("- 약정 만료일: %s\n", customer.getContractEndDate()));
        }

        if (customer.getRecencyDays() != null) {
            prompt.append(String.format("- 최근 구매 후 경과: %d일\n", customer.getRecencyDays()));
        }

        prompt.append("\n");

        if (targetProduct != null) {
            prompt.append("### [마케팅할 타겟 상품]\n");
            prompt.append(String.format("- 상품명: %s\n", targetProduct.getName()));
            prompt.append(String.format("- 카테고리: %s\n", targetProduct.getCategory()));
            prompt.append(String.format("- 가격: %,d원\n", targetProduct.getPrice().intValue()));

            if (targetProduct.getBenefits() != null) {
                prompt.append(String.format("- 혜택: %s\n", targetProduct.getBenefits()));
            }

            prompt.append("\n⚠️ **중요**: 위 타겟 상품을 마케팅하기에 가장 효과적인 캠페인을 추천해주세요.\n\n");
        }

        // 활성 캠페인 목록
        prompt.append("### [현재 진행 중인 캠페인 목록]\n");
        int index = 1;
        for (Campaign campaign : campaigns) {
            prompt.append(String.format("%d. **캠페인 ID**: %d\n", index++, campaign.getCampaignId()));
            prompt.append(String.format("   - 캠페인명: %s\n", campaign.getName()));
            prompt.append(String.format("   - 타입: %s\n", campaign.getType().getDisplayName()));

            if (campaign.getDescription() != null) {
                prompt.append(String.format("   - 혜택: %s\n", campaign.getDescription()));
            }

            prompt.append("\n");
        }

        prompt.append("### [추천 요청]\n");
        if (targetProduct != null) {
            prompt.append("위 고객에게 **타겟 상품을 마케팅하기 위해** 가장 효과적인 캠페인 3개를 추천해주세요.\n");
        } else {
            prompt.append("위 고객에게 가장 적합한 캠페인 3개를 추천해주세요.\n");
        }

        prompt.append("\n다음 형식의 JSON 배열로만 응답해주세요:\n\n");
        prompt.append("```json\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"rank\": 1,\n");
        prompt.append("    \"campaignId\": 10,\n");
        prompt.append("    \"reason\": \"이 고객에게 이 캠페인이 적합한 이유를 2-3문장으로 구체적으로 설명\",\n");
        prompt.append("    \"expectedBenefit\": \"예상 혜택 (금액이나 구체적인 내용)\",\n");
        prompt.append("    \"relevanceScore\": 95\n");
        prompt.append("  },\n");
        prompt.append("  ...\n");
        prompt.append("]\n");
        prompt.append("```\n\n");

        prompt.append("**중요**:\n");
        prompt.append("- campaignId는 위 목록에 있는 ID만 사용\n");
        prompt.append("- relevanceScore는 1~100 사이 정수\n");
        prompt.append("- reason은 고객의 현재 상황을 반영한 구체적인 설명\n");
        prompt.append("- 반드시 JSON 배열 형식으로만 응답 (다른 텍스트 포함 금지)\n");

        return prompt.toString();
    }


    private List<AIRecommendedCampaign> callOpenAIForCampaignRecommendation(String prompt) {
        try {
            OpenAIRequest request = OpenAIRequest.builder()
                    .model(openAIProperties.getModel())
                    .messages(List.of(
                            OpenAIMessage.builder()
                                    .role("system")
                                    .content("당신은 KT의 개인화 마케팅 캠페인 추천 전문가입니다. JSON 형식으로만 응답합니다.")
                                    .build(),
                            OpenAIMessage.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .temperature(0.7)
                    .maxTokens(1500)
                    .build();

            OpenAIResponse response = openAIService.callChatCompletion(request);
            String content = response.getChoices().get(0).getMessage().getContent();

            log.debug("OpenAI 응답:\n{}", content);

            // JSON 파싱
            String cleanedContent = content
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            List<AIRecommendedCampaign> recommendations = objectMapper.readValue(
                    cleanedContent,
                    new TypeReference<List<AIRecommendedCampaign>>() {
                    }
            );

            log.info("AI 추천 결과: {}개 캠페인", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("캠페인 추천 중 오류 발생", e);
            throw new BusinessException(ErrorCode.MESSAGE_GENERATION_FAILED);
        }
    }

    private CustomerProfileSummary buildCustomerProfileSummary(Customer customer) {
        return CustomerProfileSummary.builder()
                .age(customer.getAge())
                .gender(customer.getGender() != null ? customer.getGender().name() : null)
                .membershipLevel(customer.getMembershipLevel() != null ?
                        customer.getMembershipLevel().name() : null)
                .currentPlan(customer.getCurrentPlan())
                .avgDataUsage(customer.getAvgDataUsageGb() != null ?
                        String.format("%.1fGB", customer.getAvgDataUsageGb()) : null)
                .contractEndDate(customer.getContractEndDate() != null ?
                        customer.getContractEndDate().toString() : null)
                .daysSinceLastPurchase(customer.getRecencyDays())
                .build();
    }


    private TargetProductInfo buildTargetProductInfo(Product product) {
        return TargetProductInfo.builder()
                .productId(product.getProductId())
                .productName(product.getName())
                .category(product.getCategory())
                .price(product.getPrice().intValue())
                .build();
    }

    private Customer findCustomerById(Long customerId) {
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}