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
import java.time.temporal.ChronoUnit;
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

        Product targetProduct = productId != null ? findProductById(productId) : null;
        String prompt = null;
        if (targetProduct != null) {
            log.info("타겟 상품: {}", targetProduct.getName());
            prompt = buildCampaignRecommendationPromptWithProduct(customer, activeCampaigns, targetProduct);
        } else {
            prompt = buildCampaignRecommendationPrompt(customer, activeCampaigns);
            log.info("타겟 상품 없음");
        }

        log.info("생성된 프롬프트:\n{}", prompt);

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


    private String buildCampaignRecommendationPromptWithProduct(
            Customer customer,
            List<Campaign> campaigns,
            Product targetProduct) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 KT의 마케팅 전문가입니다.\n");
        prompt.append("고객 정보와 활성 캠페인 목록을 분석하여 최적의 캠페인 3개를 추천해주세요.\n\n");

        prompt.append("🎯 **핵심 미션**: 아래 타겟 상품과 고객을 위한 최적 캠페인을 찾아주세요!\n\n");

        prompt.append("### 📦 타겟 상품 정보 (매우 중요)\n");
        prompt.append(String.format("- 상품명: %s\n", targetProduct.getName()));
        prompt.append(String.format("- 카테고리: %s\n", targetProduct.getCategory()));
        prompt.append(String.format("- 가격: %,d원\n", targetProduct.getPrice().intValue()));
        if (targetProduct.getBenefits() != null) {
            prompt.append(String.format("- 핵심 혜택:\n%s\n", formatBenefits(targetProduct.getBenefits())));
        }
        prompt.append("\n");

        getCustomerProfileInfoToJson(customer, prompt);
        getActiveCampaignsInfoToJson(campaigns, prompt);

        prompt.append("⚖️ **추천 균형 원칙 (반드시 준수)**:\n");
        prompt.append("- 상품 연관성: 50% - 이 상품과 직접 관련된 캠페인인가?\n");
        prompt.append("- 고객 적합성: 50% - 이 고객에게도 적합한 캠페인인가?\n");
        prompt.append("→ 두 요소를 균형있게 고려하여 추천하세요.\n\n");

        prompt.append("### 🎯 추천 기준 (반드시 준수)\n\n");

        prompt.append("#### 1. 논리적 적합성 검증 (필수)\n");
        prompt.append("추천 전에 다음을 반드시 확인하세요:\n");
        prompt.append("- 상품 타겟 연령/조건이 고객과 맞는가?\n");
        prompt.append("- 캠페인 대상이 고객과 맞는가?\n");
        prompt.append("#### 2️. reason 작성 3단계 (구체적으로)\n\n");
        prompt.append("**[1단계] 고객의 현재 상황 분석**\n");
        prompt.append("- 강민수님은 46세, VIP, 광주 거주, 13년 이용, 5G 시그니처 사용\n");
        prompt.append("- 데이터 63.7GB 사용 (헤비 유저)\n");
        prompt.append("- 959일 동안 미구매 → 이탈 위험\n\n");
        prompt.append("**[2단계] 상품의 핵심 특징 파악**\n");
        prompt.append(String.format("- %s: %s 카테고리\n",
                targetProduct.getName(), targetProduct.getCategory()));
        if (targetProduct.getBenefits() != null) {
            prompt.append("- 상품 혜택:\n");
            String[] benefits = targetProduct.getBenefits().split("[,/]");
            for (String b : benefits) {
                prompt.append(String.format("  • %s\n", b.trim()));
            }
        }
        prompt.append("\n");
        prompt.append("**[3단계] 연결고리 명확히 설명**\n");
        prompt.append("reason에 반드시 포함할 내용:\n");
        prompt.append("1. 이 상품이 고객에게 왜 필요한가? (구체적 근거)\n");
        prompt.append("2. 이 캠페인이 왜 이 상품 구매를 도와주는가? (할인/혜택)\n");
        prompt.append("3. 두 가지가 결합되면 고객에게 무엇이 좋은가? (시너지)\n\n");

        String exampleReason = String.format(
                "000님은 00세 VIP 고객으로 0000를 사용 중이며 월 00GB의 데이터를 소비하는 유저입니다. " +
                        "%s 상품은 [상품의 구체적 특징]을 제공하며, " +
                        "이 캠페인의 [캠페인 혜택 구체적 명시]를 통해 " +
                        "[고객이 얻는 실질적 이득]을 누릴 수 있습니다.",
                targetProduct.getName()
        );

        prompt.append(buildCampaignResponseCommonFormat(true, exampleReason));
        return prompt.toString();
    }

    private void getCustomerProfileInfoToJson(Customer customer, StringBuilder prompt) {
        prompt.append("## 📊 고객 프로필\n");
        prompt.append(String.format("- **이름**: %s\n", customer.getName()));
        prompt.append(String.format("- **나이/성별**: %d세 %s\n",
                customer.getAge(),
                customer.getGender() != null ? customer.getGender().getDescription() : "미지정"));
        prompt.append(String.format("- **멤버십**: %s 등급\n",
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "미지정"));

        if (customer.getJoinDate() != null) {
            long yearsAsCustomer = java.time.temporal.ChronoUnit.YEARS.between(
                    customer.getJoinDate(),
                    java.time.LocalDateTime.now()
            );
            prompt.append(String.format("- **가입일**: %s (%d년 이용 고객)\n",
                    customer.getJoinDate().toLocalDate(), yearsAsCustomer));
        }

        if (customer.getRegion() != null) {
            prompt.append(String.format("- **거주 지역**: %s\n",
                    customer.getRegion().getDescription()));
        }

        prompt.append(String.format("- **현재 요금제**: %s\n", customer.getCurrentPlan()));

        if (customer.getCurrentDevice() != null) {
            prompt.append(String.format("- **현재 기기**: %s\n", customer.getCurrentDevice()));
        }

        prompt.append(String.format("- **데이터 사용량**: %.1fGB (월평균)\n",
                customer.getAvgDataUsageGb()));

        if (customer.getRecencyDays() != null) {
            prompt.append(String.format("- **마지막 구매**: %d일 전\n",
                    customer.getRecencyDays()));
        }
        if (customer.getContractEndDate() != null) {
            prompt.append(String.format("- **약정 종료일**: %s\n",
                    customer.getContractEndDate()));
        }
        prompt.append("\n");
    }

    private String buildCampaignRecommendationPrompt(Customer customer, List<Campaign> campaigns) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 KT의 마케팅 전문가입니다.\n");
        prompt.append("고객 정보와 활성 캠페인 목록을 분석하여 최적의 캠페인 3개를 추천해주세요.\n\n");

        prompt.append("🎯 **핵심 미션**: 아래의 고객 프로필과 활성 캠페인 정보를 분석하여 최적 캠페인을 찾아주세요!\n\n");

        getCustomerProfileInfoToJson(customer, prompt);
        getActiveCampaignsInfoToJson(campaigns, prompt);

        prompt.append("\n## 🎯 추천 기준\n\n");
        prompt.append("**추천 시 반드시 고려할 점:**\n");
        prompt.append("1. **고객의 현재 상태를 구체적으로 언급**하세요\n");
        prompt.append("   - 예: \"000 고객은 5G 시그니처 요금제를 사용중이며...\"\n");
        prompt.append("   - 예: \"VIP 등급으로서 프리미엄 서비스 선호도가 높으므로...\"\n\n");

        prompt.append("2. **reason 작성 시 필수 포함 요소:**\n");
        prompt.append("   - 고객의 이름\n");
        prompt.append("   - 고객의 구체적 상황 (요금제, 멤버십, 사용 패턴 등)\n");
        prompt.append("   - 이 캠페인이 **왜 이 고객에게** 적합한지 개인화된 설명\n\n");

        prompt.append("3. **일반적 마케팅 용어 지양:**\n");
        prompt.append("   - ❌ \"고객의 구매욕구를 자극\"\n");
        prompt.append("   - ❌ \"고객유지 효과 기대\"\n");
        prompt.append("   - ✅ \"000 고객님의 [구체적 상황]을 고려할 때...\"\n\n");

        String exampleReason = String.format(
                "%s 고객은 %s 등급이며 %s 요금제를 사용중입니다. 이 캠페인은 고객의 현재 상황에 매우 적합하며...",
                customer.getName(),
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "회원",
                customer.getCurrentPlan() != null ? customer.getCurrentPlan() : "현재"
        );

        prompt.append(buildCampaignResponseCommonFormat(false, exampleReason));
        return prompt.toString();
    }

    private void getActiveCampaignsInfoToJson(List<Campaign> campaigns, StringBuilder prompt) {
        prompt.append("### 📋 활성 캠페인 목록\n");
        for (int i = 0; i < campaigns.size(); i++) {
            Campaign c = campaigns.get(i);
            prompt.append(String.format("%d. [ID:%d] %s (%s)\n",
                    i + 1, c.getCampaignId(), c.getName(), c.getType().getDisplayName()));
            if (c.getDescription() != null) {
                prompt.append(String.format("   혜택: %s\n", c.getDescription()));
            }
        }
        prompt.append("\n");
    }

    private String buildCampaignResponseCommonFormat(boolean withProduct, String exampleReason) {
        StringBuilder format = new StringBuilder();
        format.append("### 📤 응답 형식 (JSON만 출력, 다른 텍스트 금지)\n");
        format.append("[\n");
        format.append("  {\n");
        format.append("    \"rank\": 순위,\n");
        format.append("    \"campaignId\": 캠페인아이디,\n");
        format.append(String.format("    \"reason\": \"%s\",\n", exampleReason));
        format.append("    \"expectedBenefit\": \"예상 혜택\",\n");
        format.append("    \"relevanceScore\": 연관도 점수\n");
        format.append("  },\n");
        format.append("  {\n");
        format.append("    \"rank\": 순위,\n");
        format.append("    \"campaignId\": 캠페인아이디,\n");
        format.append(String.format("    \"reason\": \"%s\",\n", exampleReason));
        format.append("    \"expectedBenefit\": \"...\",\n");
        format.append("    \"relevanceScore\": 연관도 점수\n");
        format.append("  },\n");
        format.append("  {\n");
        format.append("    \"rank\": 순위,\n");
        format.append("    \"campaignId\": 캠페인아이디,\n");
        format.append(String.format("    \"reason\": \"%s\",\n", exampleReason));
        format.append("    \"expectedBenefit\": \"...\",\n");
        format.append("    \"relevanceScore\": 연관도 점수\n");
        format.append("  }\n");
        format.append("]\n");
        format.append("\n");
        format.append("- **rank**: 1 (최우선), 2, 3 순서대로 부여\n");
        format.append("- relevanceScore: 85~100 사이 점수\n");
        format.append("```\n\n");

        format.append("### ✅ 응답 규칙\n");
        format.append("- **rank**: 1 (최우선), 2, 3 순서대로 부여 (필수)\n");
        format.append("- **campaignId**: 위 캠페인 목록의 ID 중 선택\n");
        format.append("- **relevanceScore**: 85~100 사이 점수\n");

        if (withProduct) {
            format.append("- **reason**: 타겟 상품 연관성(50%) + 고객 적합성(50%) 모두 명시\n");
            format.append("  → 반드시 상품명 포함 + 상품-캠페인 연결고리 설명\n");
        } else {
            format.append("- **reason**: 고객의 이름과 구체적 상황을 포함한 개인화된 설명\n");
            format.append("  → 일반적 마케팅 용어 지양, 이 고객만의 맞춤 이유 설명\n");
        }

        format.append("- **expectedBenefit**: 고객이 실제 받을 수 있는 혜택\n");
        format.append("- 반드시 3개 캠페인 추천 (더 많거나 적으면 안됨)\n");

        return format.toString();
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

            log.info("OpenAI 응답:\n{}", content);

            List<AIRecommendedCampaign> recommendations = parseAIResponseOfCampaign(content);

            log.info("AI 추천 결과: {}개 캠페인", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("캠페인 추천 중 오류 발생", e);
            throw new BusinessException(ErrorCode.RECOMMENDATION_FAILED);
        }
    }

    private CustomerProfileSummary buildCustomerProfileSummary(Customer customer) {
        Integer yearsAsCustomer = null;
        String joinDate = null;

        if (customer.getJoinDate() != null) {
            yearsAsCustomer = Math.toIntExact(ChronoUnit.YEARS.between(customer.getJoinDate(), LocalDateTime.now()));
            joinDate = customer.getJoinDate().toLocalDate().toString();
        }

        return CustomerProfileSummary.builder()
                .age(customer.getAge())
                .gender(customer.getGender() != null ? customer.getGender().name() : null)
                .membershipLevel(customer.getMembershipLevel() != null ?
                        customer.getMembershipLevel().name() : null)
                .joinDate(joinDate)
                .yearsAsCustomer(yearsAsCustomer)
                .region(customer.getRegion() != null ?
                        customer.getRegion().getDescription() : null)
                .currentDevice(customer.getCurrentDevice())
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

    private String formatBenefits(String benefits) {
        if (benefits == null || benefits.isEmpty()) {
            return "  (혜택 정보 없음)";
        }

        String[] lines = benefits.split("[,/\n]");
        StringBuilder formatted = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                formatted.append("  • ").append(trimmed).append("\n");
            }
        }
        return formatted.toString();
    }

    private List<AIRecommendedCampaign> parseAIResponseOfCampaign(String content) {
        try {
            String cleanedContent = content
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            return objectMapper.readValue(
                    cleanedContent,
                    new TypeReference<>() {
                    }
            );

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패 - content: {}", content, e);
            throw new BusinessException(ErrorCode.INVALID_JSON_RESPONSE);
        }
    }
}