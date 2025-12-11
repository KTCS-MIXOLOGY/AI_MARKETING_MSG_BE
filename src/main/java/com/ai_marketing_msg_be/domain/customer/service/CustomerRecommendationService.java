package com.ai_marketing_msg_be.domain.customer.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.entity.CampaignStatus;
import com.ai_marketing_msg_be.domain.campaign.repository.CampaignRepository;
import com.ai_marketing_msg_be.domain.customer.dto.AIRecommendedCampaign;
import com.ai_marketing_msg_be.domain.customer.dto.AIRecommendedProduct;
import com.ai_marketing_msg_be.domain.customer.dto.CampaignRecommendationResponse;
import com.ai_marketing_msg_be.domain.customer.dto.CampaignRecommendationResponse.CustomerProfileSummary;
import com.ai_marketing_msg_be.domain.customer.dto.CampaignRecommendationResponse.TargetProductInfo;
import com.ai_marketing_msg_be.domain.customer.dto.ProductRecommendationResponse;
import com.ai_marketing_msg_be.domain.customer.dto.ProductRecommendationResponse.TargetCampaignInfo;
import com.ai_marketing_msg_be.domain.customer.dto.RecommendedCampaign;
import com.ai_marketing_msg_be.domain.customer.dto.RecommendedProduct;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public ProductRecommendationResponse recommendProducts(Long customerId, Long campaignId) {
        log.info("상품 추천 요청 - customerId: {}, campaignId: {}", customerId, campaignId);

        Customer customer = findCustomerById(customerId);
        log.info("고객 조회 완료 - name: {}, age: {}, membership: {}",
                customer.getName(), customer.getAge(), customer.getMembershipLevel());

        List<Product> availableProducts = productRepository.findAvailableProducts();
        log.info("재고 있는 상품 조회 완료 - 총 {}개", availableProducts.size());

        if (availableProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                    "추천 가능한 상품이 없습니다.");
        }

        List<Product> eligibleProducts = filterProductsByAge(availableProducts, customer.getAge());
        log.info("필터링 후 상품 수: {}개 (원본: {}개)",
                eligibleProducts.size(), availableProducts.size());

        if (eligibleProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                    String.format("%d세 고객에게 추천 가능한 상품이 없습니다.", customer.getAge()));
        }

        Campaign targetCampaign = null;
        if (campaignId != null) {
            targetCampaign = findCampaignById(campaignId);
            log.info("타겟 캠페인 조회 완료 - name: {}, type: {}",
                    targetCampaign.getName(), targetCampaign.getType());
        }

        String prompt = targetCampaign != null
                ? buildProductRecommendationPromptWithCampaign(customer, eligibleProducts, targetCampaign)
                : buildProductRecommendationPrompt(customer, eligibleProducts);

        log.info("생성된 프롬프트:\n{}", prompt);

        List<AIRecommendedProduct> aiRecommendations = callOpenAIForProductRecommendation(prompt);
        log.info("AI 추천 완료 - 추천 상품 수: {}", aiRecommendations.size());

        List<RecommendedProduct> recommendations = mapToRecommendedProducts(
                aiRecommendations, eligibleProducts, customer.getAge());

        log.info("상품 추천 완료 - customerId: {}, 추천 상품 수: {}",
                customerId, recommendations.size());

        return ProductRecommendationResponse.builder()
                .customerId(customerId)
                .customerName(customer.getName())
                .customerProfile(buildCustomerProfileSummaryForProduct(customer))
                .targetCampaign(targetCampaign != null ? buildTargetCampaignInfo(targetCampaign) : null)
                .recommendations(recommendations)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private List<Product> filterProductsByAge(List<Product> products, Integer customerAge) {
        if (customerAge == null) {
            log.warn("고객 나이 정보 없음 - 필터링 없이 모든 상품 반환");
            return products;
        }

        List<Product> filtered = new ArrayList<>();

        Pattern maxAgePattern = Pattern.compile("만\\s*(\\d+)세\\s*이하");
        Pattern minAgePattern = Pattern.compile("만\\s*(\\d+)세\\s*이상");
        Pattern rangePattern = Pattern.compile("만\\s*(\\d+)세?\\s*~\\s*(\\d+)세");

        for (Product product : products) {
            String productName = product.getName();
            String benefits = product.getBenefits() != null ? product.getBenefits() : "";
            boolean isEligible = true;
            String filterReason = "";

            if (productName.contains("군인") || benefits.contains("군인")) {
                isEligible = false;
                filterReason = "군인 전용 상품 (고객 정보 미확인)";
            }

            if (productName.contains("외국인") || benefits.contains("외국인")) {
                isEligible = false;
                filterReason = "외국인 전용 상품 (고객 정보 미확인)";
            }

            if ((productName.contains("장애인") || benefits.contains("장애인") ||
                    productName.contains("복지") || benefits.contains("복지") ||
                    productName.contains("국가유공자") || benefits.contains("국가유공자")) &&
                    (productName.contains("전용") || benefits.contains("전용"))) {
                isEligible = false;
                filterReason = "복지 대상자 전용 상품 (고객 정보 미확인)";
            }

            if (isEligible) {
                Matcher maxAgeMatcher = maxAgePattern.matcher(productName);
                if (maxAgeMatcher.find()) {
                    int maxAge = Integer.parseInt(maxAgeMatcher.group(1));
                    if (customerAge > maxAge) {
                        isEligible = false;
                        filterReason = String.format("최대 연령 제한(%d세) 초과", maxAge);
                    }
                }
            }

            if (isEligible) {
                Matcher minAgeMatcher = minAgePattern.matcher(productName);
                if (minAgeMatcher.find()) {
                    int minAge = Integer.parseInt(minAgeMatcher.group(1));
                    if (customerAge < minAge) {
                        isEligible = false;
                        filterReason = String.format("최소 연령 제한(%d세) 미달", minAge);
                    }
                }
            }

            if (isEligible) {
                Matcher rangeMatcher = rangePattern.matcher(productName);
                if (rangeMatcher.find()) {
                    int minAge = Integer.parseInt(rangeMatcher.group(1));
                    int maxAge = Integer.parseInt(rangeMatcher.group(2));
                    if (customerAge < minAge || customerAge > maxAge) {
                        isEligible = false;
                        filterReason = String.format("연령 범위(%d~%d세) 벗어남", minAge, maxAge);
                    }
                }
            }

            if (isEligible) {
                filtered.add(product);
            } else {
                log.debug("상품 필터링: [{}] - 사유: {}", productName, filterReason);
            }
        }

        log.info("필터링 결과: 전체 {}개 → 적격 {}개 (고객 나이: {}세)",
                products.size(), filtered.size(), customerAge);

        return filtered;
    }

    private List<RecommendedProduct> mapToRecommendedProducts(
            List<AIRecommendedProduct> aiRecommendations,
            List<Product> eligibleProducts,
            Integer customerAge) {

        Map<Long, Product> productMap = eligibleProducts.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        List<RecommendedProduct> recommendations = new ArrayList<>();

        for (AIRecommendedProduct aiRec : aiRecommendations) {
            Product product = productMap.get(aiRec.getProductId());

            if (product == null) {
                log.warn("AI가 추천한 상품을 찾을 수 없음 - productId: {}", aiRec.getProductId());
                continue;
            }

            if (!isProductEligible(product, customerAge)) {
                log.warn("AI가 부적격 상품 추천 - productId: {}, productName: {}, customerAge: {}",
                        product.getProductId(), product.getName(), customerAge);
                continue;
            }

            RecommendedProduct recommendedProduct = RecommendedProduct.fromProduct(
                    product,
                    aiRec.getRank(),
                    aiRec.getReason(),
                    aiRec.getExpectedBenefit(),
                    aiRec.getRelevanceScore()
            );

            recommendations.add(recommendedProduct);
        }

        if (recommendations.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                    "추천 가능한 상품을 찾을 수 없습니다.");
        }

        recommendations.sort((a, b) -> Integer.compare(a.getRank(), b.getRank()));

        return recommendations;
    }

    private boolean isProductEligible(Product product, Integer customerAge) {
        if (customerAge == null) {
            return true;
        }

        String productName = product.getName();
        String benefits = product.getBenefits() != null ? product.getBenefits() : "";

        if (productName.contains("군인") || benefits.contains("군인")) {
            return false;
        }

        if (productName.contains("외국인") || benefits.contains("외국인")) {
            return false;
        }

        if ((productName.contains("장애인") || benefits.contains("장애인") ||
                productName.contains("복지") || benefits.contains("복지") ||
                productName.contains("국가유공자") || benefits.contains("국가유공자")) &&
                (productName.contains("전용") || benefits.contains("전용"))) {
            return false;
        }

        Pattern maxAgePattern = Pattern.compile("만\\s*(\\d+)세\\s*이하");
        Pattern minAgePattern = Pattern.compile("만\\s*(\\d+)세\\s*이상");
        Pattern rangePattern = Pattern.compile("만\\s*(\\d+)세?\\s*~\\s*(\\d+)세");

        Matcher maxAgeMatcher = maxAgePattern.matcher(productName);
        if (maxAgeMatcher.find()) {
            int maxAge = Integer.parseInt(maxAgeMatcher.group(1));
            if (customerAge > maxAge) {
                return false;
            }
        }

        Matcher minAgeMatcher = minAgePattern.matcher(productName);
        if (minAgeMatcher.find()) {
            int minAge = Integer.parseInt(minAgeMatcher.group(1));
            if (customerAge < minAge) {
                return false;
            }
        }

        Matcher rangeMatcher = rangePattern.matcher(productName);
        if (rangeMatcher.find()) {
            int minAge = Integer.parseInt(rangeMatcher.group(1));
            int maxAge = Integer.parseInt(rangeMatcher.group(2));
            if (customerAge < minAge || customerAge > maxAge) {
                return false;
            }
        }

        return true;
    }


    private String buildProductRecommendationPrompt(Customer customer, List<Product> products) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 KT의 전문 상품 추천 컨설턴트입니다.\n");
        prompt.append("고객의 프로필과 현재 이용 상황을 심층 분석하여 가장 적합한 상품 3가지를 추천해주세요.\n\n");

        prompt.append("🎯 **핵심 미션**: 아래 고객을 위한 최적의 상품 3가지를 찾아주세요!\n\n");

        prompt.append(String.format("⚠️ **중요**: 이 고객은 **%d세**입니다. ", customer.getAge()));
        prompt.append("상품명에 연령 제한이 있으면 절대 준수하세요!\n\n");

        prompt.append("🚫 **절대 추천 금지 상품**:\n");
        prompt.append("- 군인 전용 상품 (고객의 군인 여부 정보 없음)\n");
        prompt.append("- 외국인 전용 상품 (고객의 국적 정보 없음)\n");
        prompt.append("- 장애인/복지 대상자 전용 상품 (고객의 복지 대상 여부 정보 없음)\n");
        prompt.append("→ 이러한 상품은 이미 필터링되었으므로 목록에 없습니다.\n\n");

        getCustomerProfileInfoToJson(customer, prompt);

        prompt.append("### 📋 추천 가능 상품 목록\n");
        prompt.append("✅ 아래 상품들은 이미 연령 및 특수 조건 필터링을 거쳤습니다.\n");
        prompt.append(buildDetailedProductListInfo(products));
        prompt.append("\n");

        prompt.append("## 🎯 추천 기준 (반드시 준수)\n\n");

        prompt.append("#### 1. 논리적 적합성 검증 (필수)\n");
        prompt.append("추천 전에 다음을 반드시 확인하세요:\n");
        prompt.append(String.format("- ⚠️ **이 고객은 %d세입니다!** 상품명에 연령 제한이 있으면 절대 추천 금지!\n",
                customer.getAge()));

        if (customer.getCurrentPlan() != null) {
            String currentPlan = customer.getCurrentPlan();
            if (currentPlan.contains("5G")) {
                prompt.append(String.format("- 🚨 **다운그레이드 금지**: 현재 '%s' 사용 중 → LTE나 3G 요금제 추천 절대 금지!\n", currentPlan));
                prompt.append("- 5G 요금제 또는 동급 이상 상품만 추천 가능\n");
            } else if (currentPlan.contains("LTE")) {
                prompt.append(String.format("- 🚨 **다운그레이드 금지**: 현재 '%s' 사용 중 → 3G 요금제 추천 절대 금지!\n", currentPlan));
                prompt.append("- LTE 요금제 또는 5G 업그레이드 상품 추천 가능\n");
            }
        }

        if (customer.getMembershipLevel() != null) {
            String membership = customer.getMembershipLevel().getDescription();
            if (membership.contains("VIP") || membership.contains("VVIP")) {
                prompt.append(
                        String.format("- 💎 **프리미엄 고객**: %s 등급 → 저가형 상품(슬림/베이직) 추천 지양, 프리미엄/시그니처급 우선\n", membership));
            }
        }

        if (customer.getAvgDataUsageGb() != null && customer.getAvgDataUsageGb().compareTo(new BigDecimal("50")) > 0) {
            prompt.append(
                    String.format("- 📊 **헤비 유저**: 월 %.1fGB 사용 → 대용량/무제한 데이터 요금제 필수\n", customer.getAvgDataUsageGb()));
        }

        if (customer.getAvgDataUsageGb() != null) {
            prompt.append(String.format("- 💾 **데이터 사용량 검증 필수**: 추천 요금제의 데이터 제공량이 %.1fGB 이상이어야 함\n",
                    customer.getAvgDataUsageGb()));
            prompt.append("  (무제한 요금제는 자동 통과, 요고 다이렉트 같은 소용량 요금제는 사용량 부족 시 추천 금지)\n");
        }

        if (customer.getMembershipLevel() != null && customer.getCurrentPlan() != null) {
            String membership = customer.getMembershipLevel().name();
            String currentPlan = customer.getCurrentPlan();

            prompt.append("- 💰 **멤버십별 가격대 제한** (모바일 요금제 한정):\n");

            if (membership.equals("WHITE") || membership.equals("BASIC")) {
                prompt.append(String.format("  WHITE/BASIC 등급 → 현재 요금제(%s) 기준 ±20%% 가격대 내 추천 권장\n", currentPlan));
                prompt.append("  (예: 5만원 요금제 → 4만~6만원대 추천, 급격한 업셀링 지양)\n");
            } else if (membership.equals("SILVER") || membership.equals("GOLD")) {
                prompt.append(String.format("  SILVER/GOLD 등급 → 현재 요금제(%s) 기준 ±30%% 가격대 내 추천 권장\n", currentPlan));
                prompt.append("  (적당한 업셀링 가능)\n");
            } else if (membership.contains("VIP")) {
                prompt.append(String.format("  VIP/VVIP 등급 → 프리미엄 고객이므로 가격대 제한 없음\n"));
                prompt.append("  (고가 요금제 자유롭게 추천 가능)\n");
            }
        }

        prompt.append("- 모바일 카테고리 상품이라면 위 조건들을 철저히 검토\n");
        prompt.append("- 기타 카테고리(OTT, 디바이스, 생활편의 등)는 고객 프로필에 맞춰 자유롭게 추천\n\n");

        prompt.append("#### 2. reason 작성 3단계 (구체적으로)\n\n");
        prompt.append("**[1단계] 고객의 현재 상황 분석**\n");
        prompt.append(String.format("- %s님은 %d세, %s, %s 거주\n",
                customer.getName(),
                customer.getAge(),
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "일반",
                customer.getRegion() != null ? customer.getRegion().getDescription() : ""));

        if (customer.getJoinDate() != null) {
            long yearsAsCustomer = ChronoUnit.YEARS.between(customer.getJoinDate(), LocalDateTime.now());
            prompt.append(String.format("- %d년 이용 고객\n", yearsAsCustomer));
        }

        if (customer.getCurrentPlan() != null) {
            prompt.append(String.format("- 현재 %s 사용 중\n", customer.getCurrentPlan()));
        }

        if (customer.getAvgDataUsageGb() != null) {
            prompt.append(String.format("- 데이터 %.1fGB 사용\n", customer.getAvgDataUsageGb()));
        }

        if (customer.getRecencyDays() != null) {
            prompt.append(String.format("- %d일 동안 미구매\n", customer.getRecencyDays()));
        }
        prompt.append("\n");

        prompt.append("**[2단계] 상품의 핵심 가치 파악**\n");
        prompt.append("- 이 상품이 제공하는 핵심 혜택은 무엇인가?\n");
        prompt.append("- 이 상품의 타겟 고객층은 누구인가?\n");
        prompt.append("- 가격 대비 제공되는 가치는 충분한가?\n\n");

        prompt.append("**[3단계] 연결고리 명확히 설명**\n");
        prompt.append("reason에 반드시 포함할 내용:\n");
        prompt.append("1. 이 상품이 **왜 이 고객에게** 필요한가? (구체적 근거)\n");
        prompt.append("2. 고객의 현재 상황에서 이 상품이 어떤 문제를 해결하는가?\n");
        prompt.append("3. 이 상품을 통해 고객이 얻는 실질적 이익은 무엇인가?\n\n");

        String exampleReason = buildProductRecommendationExampleReason(customer);

        prompt.append("**reason 예시:**\n");
        prompt.append(String.format("\"%s\"\n\n", exampleReason));

        prompt.append(buildProductResponseFormat(exampleReason));

        return prompt.toString();
    }

    private String buildProductRecommendationPromptWithCampaign(
            Customer customer, List<Product> products, Campaign campaign) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 KT의 전문 상품 추천 컨설턴트입니다.\n");
        prompt.append("특정 마케팅 캠페인에 맞춰 고객에게 가장 적합한 상품 3가지를 추천해주세요.\n\n");

        prompt.append("🎯 **핵심 미션**: 아래 캠페인과 고객을 위한 최적의 상품을 찾아주세요!\n\n");

        prompt.append(String.format("⚠️ **중요**: 이 고객은 **%d세**입니다. ", customer.getAge()));
        prompt.append("상품명에 연령 제한이 있으면 절대 준수하세요!\n\n");

        prompt.append("🚫 **절대 추천 금지 상품**:\n");
        prompt.append("- 군인 전용 상품 (고객의 군인 여부 정보 없음)\n");
        prompt.append("- 외국인 전용 상품 (고객의 국적 정보 없음)\n");
        prompt.append("- 장애인/복지 대상자 전용 상품 (고객의 복지 대상 여부 정보 없음)\n");
        prompt.append("→ 이러한 상품은 이미 필터링되었으므로 목록에 없습니다.\n\n");

        prompt.append("### 🎁 타겟 마케팅 캠페인 (매우 중요)\n");
        prompt.append(String.format("- **캠페인명**: %s\n", campaign.getName()));
        prompt.append(String.format("- **캠페인 유형**: %s\n", campaign.getType().getDisplayName()));
        if (campaign.getDescription() != null && !campaign.getDescription().isEmpty()) {
            prompt.append(String.format("- **캠페인 설명**: %s\n", campaign.getDescription()));
        }
        if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
            prompt.append(String.format("- **캠페인 기간**: %s ~ %s\n",
                    campaign.getStartDate(), campaign.getEndDate()));
        }
        prompt.append("\n");

        getCustomerProfileInfoToJson(customer, prompt);

        prompt.append("### 📋 추천 가능 상품 목록\n");
        prompt.append("✅ 아래 상품들은 이미 연령 및 특수 조건 필터링을 거쳤습니다.\n");
        prompt.append(buildDetailedProductListInfo(products));
        prompt.append("\n");

        prompt.append("## 🎯 추천 기준 (반드시 준수)\n\n");

        prompt.append("⚖️ **추천 균형 원칙**:\n");
        prompt.append("- 캠페인 목적 부합도: 50%\n");
        prompt.append("- 고객 프로필 적합도: 50%\n");
        prompt.append("→ 두 요소를 균형있게 고려하여 추천하세요.\n\n");

        prompt.append("#### 1. 논리적 적합성 검증 (필수)\n");
        prompt.append("추천 전에 다음을 반드시 확인하세요:\n");
        prompt.append("- 이 상품이 캠페인 목적(신규유치/고객유지/업셀링 등)에 부합하는가?\n");
        prompt.append(String.format("- ⚠️ **이 고객은 %d세입니다!** 상품명에 연령 제한이 있으면 절대 추천 금지!\n",
                customer.getAge()));

        if (customer.getCurrentPlan() != null) {
            String currentPlan = customer.getCurrentPlan();
            if (currentPlan.contains("5G")) {
                prompt.append(String.format("- 🚨 **다운그레이드 금지**: 현재 '%s' 사용 중 → LTE나 3G 요금제 추천 절대 금지!\n", currentPlan));
                prompt.append("- 5G 요금제 또는 동급 이상 상품만 추천 가능\n");
            } else if (currentPlan.contains("LTE")) {
                prompt.append(String.format("- 🚨 **다운그레이드 금지**: 현재 '%s' 사용 중 → 3G 요금제 추천 절대 금지!\n", currentPlan));
                prompt.append("- LTE 요금제 또는 5G 업그레이드 상품 추천 가능\n");
            }
        }

        if (customer.getMembershipLevel() != null) {
            String membership = customer.getMembershipLevel().getDescription();
            if (membership.contains("VIP") || membership.contains("VVIP")) {
                prompt.append(
                        String.format("- 💎 **프리미엄 고객**: %s 등급 → 저가형 상품(슬림/베이직) 추천 지양, 프리미엄/시그니처급 우선\n", membership));
            }
        }

        if (customer.getAvgDataUsageGb() != null && customer.getAvgDataUsageGb().compareTo(new BigDecimal("50")) > 0) {
            prompt.append(
                    String.format("- 📊 **헤비 유저**: 월 %.1fGB 사용 → 대용량/무제한 데이터 요금제 필수\n", customer.getAvgDataUsageGb()));
        }

        if (customer.getAvgDataUsageGb() != null) {
            prompt.append(String.format("- 💾 **데이터 사용량 검증 필수**: 추천 요금제의 데이터 제공량이 %.1fGB 이상이어야 함\n",
                    customer.getAvgDataUsageGb()));
            prompt.append("  (무제한 요금제는 자동 통과, 요고 다이렉트 같은 소용량 요금제는 사용량 부족 시 추천 금지)\n");
        }

        if (customer.getMembershipLevel() != null && customer.getCurrentPlan() != null) {
            String membership = customer.getMembershipLevel().name();
            String currentPlan = customer.getCurrentPlan();

            prompt.append("- 💰 **멤버십별 가격대 제한** (모바일 요금제 한정):\n");

            if (membership.equals("WHITE") || membership.equals("BASIC")) {
                prompt.append(String.format("  WHITE/BASIC 등급 → 현재 요금제(%s) 기준 ±20%% 가격대 내 추천 권장\n", currentPlan));
                prompt.append("  (예: 5만원 요금제 → 4만~6만원대 추천, 급격한 업셀링 지양)\n");
            } else if (membership.equals("SILVER") || membership.equals("GOLD")) {
                prompt.append(String.format("  SILVER/GOLD 등급 → 현재 요금제(%s) 기준 ±30%% 가격대 내 추천 권장\n", currentPlan));
                prompt.append("  (적당한 업셀링 가능)\n");
            } else if (membership.contains("VIP")) {
                prompt.append(String.format("  VIP/VVIP 등급 → 프리미엄 고객이므로 가격대 제한 없음\n"));
                prompt.append("  (고가 요금제 자유롭게 추천 가능)\n");
            }
        }

        prompt.append("- 모바일 카테고리 상품이라면 위 조건들을 철저히 검토\n");
        prompt.append("- 기타 카테고리(OTT, 디바이스, 생활편의 등)는 고객 프로필에 맞춰 자유롭게 추천\n");
        prompt.append("- 고객의 현재 상황에서 캠페인 목표 달성 가능성이 있는가?\n\n");

        prompt.append("#### 2. reason 작성 3단계 (구체적으로)\n\n");
        prompt.append("**[1단계] 고객의 현재 상황 분석**\n");
        prompt.append(String.format("- %s님은 %d세, %s 등급, %s 거주\n",
                customer.getName(),
                customer.getAge(),
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "일반",
                customer.getRegion() != null ? customer.getRegion().getDescription() : ""));

        if (customer.getJoinDate() != null) {
            long yearsAsCustomer = ChronoUnit.YEARS.between(customer.getJoinDate(), LocalDateTime.now());
            prompt.append(String.format("- %d년 이용 고객\n", yearsAsCustomer));
        }

        if (customer.getCurrentPlan() != null) {
            prompt.append(String.format("- 현재 %s 사용 중\n", customer.getCurrentPlan()));
        }

        if (customer.getRecencyDays() != null) {
            prompt.append(String.format("- %d일 동안 미구매 → %s\n",
                    customer.getRecencyDays(),
                    customer.getRecencyDays() > 365 ? "이탈 위험" : "활동 중"));
        }
        prompt.append("\n");

        prompt.append("**[2단계] 캠페인-상품 연결고리 파악**\n");
        prompt.append(String.format("- 이 캠페인(%s)의 목적은 무엇인가?\n", campaign.getType().getDisplayName()));
        prompt.append("- 이 상품이 캠페인 목표 달성에 어떻게 기여하는가?\n");
        prompt.append("- 고객의 현재 상황에서 이 조합이 효과적인가?\n\n");

        prompt.append("**[3단계] 종합 설명 (reason 작성)**\n");
        prompt.append("reason에 반드시 포함할 내용:\n");
        prompt.append("1. 캠페인 목적과 이 상품의 연관성 (50%)\n");
        prompt.append("2. 고객의 현재 상황에서 이 상품이 적합한 이유 (50%)\n");
        prompt.append("3. 캠페인-상품-고객의 시너지 효과\n\n");

        String exampleReason = buildProductWithCampaignExampleReason(customer, campaign);

        prompt.append("**reason 예시:**\n");
        prompt.append(String.format("\"%s\"\n\n", exampleReason));

        prompt.append(buildProductResponseFormat(exampleReason));

        return prompt.toString();
    }


    private String buildDetailedProductListInfo(List<Product> products) {
        StringBuilder info = new StringBuilder();

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            info.append(String.format("\n**[상품 %d]**\n", i + 1));
            info.append(String.format("- productId: %d\n", product.getProductId()));
            info.append(String.format("- 상품명: %s\n", product.getName()));
            info.append(String.format("- 카테고리: %s\n", product.getCategory()));

            if (product.getPrice() != null) {
                info.append(String.format("- 정상가: %,d원\n", product.getPrice().intValue()));

                if (product.getDiscountRate() != null && product.getDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal discountedPrice = product.getDiscountedPrice();
                    info.append(String.format("- 할인율: %d%%\n", product.getDiscountRate().intValue()));
                    info.append(String.format("- 할인가: %,d원\n", discountedPrice.intValue()));
                }
            }

            if (product.getBenefits() != null && !product.getBenefits().isEmpty()) {
                info.append("- 주요 혜택:\n");
                info.append(formatBenefits(product.getBenefits()));
            }
        }

        return info.toString();
    }

    private String buildProductRecommendationExampleReason(Customer customer) {
        StringBuilder reason = new StringBuilder();

        reason.append(String.format("%s 고객은 %d세 %s 등급으로 ",
                customer.getName(),
                customer.getAge(),
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "회원"));

        if (customer.getCurrentPlan() != null) {
            reason.append(String.format("%s를 사용 중이며 ", customer.getCurrentPlan()));
        }

        if (customer.getAvgDataUsageGb() != null) {
            reason.append(String.format("월 %.1fGB의 데이터를 소비하는 ", customer.getAvgDataUsageGb()));
            if (customer.getAvgDataUsageGb().compareTo(new BigDecimal("50")) > 0) {
                reason.append("헤비 ");
            }
            reason.append("유저입니다. ");
        }

        boolean isVIP = customer.getMembershipLevel() != null &&
                (customer.getMembershipLevel().getDescription().contains("VIP"));

        if (isVIP) {
            reason.append("프리미엄 고객으로서 더 나은 서비스를 추구하시는 고객입니다. ");
            reason.append("이 상품은 [상품의 프리미엄 특징]을 제공하며, ");
            reason.append("고객의 [현재 니즈]를 충족시키면서 ");
            reason.append("[업그레이드/추가 혜택]을 통해 [가치 향상 효과]를 얻을 수 있습니다.");
        } else {
            reason.append("이 상품은 [상품의 핵심 특징]을 제공하며, ");
            reason.append("고객의 [구체적 상황/니즈]를 고려할 때 ");
            reason.append("[실질적 혜택]을 통해 [기대 효과]를 얻을 수 있습니다.");
        }

        return reason.toString();
    }

    private String buildProductWithCampaignExampleReason(Customer customer, Campaign campaign) {
        StringBuilder reason = new StringBuilder();

        reason.append(String.format("%s 고객은 %d세 %s 등급으로 ",
                customer.getName(),
                customer.getAge(),
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "회원"));

        if (customer.getJoinDate() != null) {
            long years = ChronoUnit.YEARS.between(customer.getJoinDate(), LocalDateTime.now());
            reason.append(String.format("%d년 이용 고객이며 ", years));
        }

        if (customer.getCurrentPlan() != null) {
            reason.append(String.format("%s를 사용 중입니다. ", customer.getCurrentPlan()));
        }

        reason.append(String.format("'%s' 캠페인은 %s를 목표로 하며, ",
                campaign.getName(),
                campaign.getType().getDisplayName()));

        boolean isVIP = customer.getMembershipLevel() != null &&
                (customer.getMembershipLevel().getDescription().contains("VIP"));

        if (isVIP) {
            reason.append("프리미엄 고객인 점을 고려하여 ");
            reason.append("이 상품은 [프리미엄 상품 특징]을 통해 캠페인 목적에 부합하고, ");
            reason.append("고객의 [현재 프리미엄 니즈]를 충족시키면서 [캠페인 혜택 + 상품 혜택]을 통해 ");
            reason.append("[가치 극대화 효과]를 달성할 수 있습니다.");
        } else {
            reason.append("이 상품은 [상품 특징]을 통해 캠페인 목적에 부합하고, ");
            reason.append("고객의 [현재 상황]을 고려할 때 [캠페인 혜택 + 상품 혜택]을 통해 ");
            reason.append("[기대 효과]를 달성할 수 있습니다.");
        }

        return reason.toString();
    }

    private String buildProductResponseFormat(String exampleReason) {
        StringBuilder format = new StringBuilder();

        format.append("### 📤 응답 형식 (JSON만 출력, 다른 텍스트 금지)\n");
        format.append("[\n");
        format.append("  {\n");
        format.append("    \"rank\": 1,\n");
        format.append("    \"productId\": 상품ID(숫자),\n");
        format.append(String.format("    \"reason\": \"%s\",\n", exampleReason));
        format.append("    \"expectedBenefit\": \"고객이 실제 받을 수 있는 구체적 혜택\",\n");
        format.append("    \"relevanceScore\": 85-100 사이 점수\n");
        format.append("  },\n");
        format.append("  ... (총 3개 추천)\n");
        format.append("]\n\n");

        format.append("### ✅ 응답 규칙\n");
        format.append("- **rank**: 1 (최우선), 2, 3 순서대로 부여 (필수)\n");
        format.append("- **productId**: 위 상품 목록의 ID 중 선택 (반드시)\n");
        format.append("- **reason**: 고객 이름과 구체적 상황 포함한 개인화된 설명 (200자 이내)\n");
        format.append("  → 일반적 마케팅 용어 지양, 이 고객만의 맞춤 이유 설명\n");
        format.append("  → 고객의 현재 요금제, 멤버십, 사용 패턴 등 구체적 데이터 활용\n");
        format.append("- **expectedBenefit**: 이 고객이 이 상품으로 얻는 실질적 혜택 (150자 이내)\n");
        format.append("- **relevanceScore**: 고객 적합도를 정확히 반영한 85-100 사이 점수\n");
        format.append("- 반드시 3개 상품 추천 (더 많거나 적으면 안됨)\n");

        return format.toString();
    }

    private List<AIRecommendedProduct> callOpenAIForProductRecommendation(String prompt) {
        try {
            log.info("OpenAI API 호출 시작 - 상품 추천");

            OpenAIRequest request = OpenAIRequest.builder()
                    .model(openAIProperties.getModel())
                    .messages(List.of(
                            OpenAIMessage.builder()
                                    .role("system")
                                    .content(
                                            "당신은 KT의 전문 상품 추천 컨설턴트입니다. 고객 데이터를 심층 분석하여 최적의 상품을 추천합니다. JSON 형식으로만 응답합니다.")
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

            return parseAIResponseOfProduct(content);

        } catch (Exception e) {
            log.error("OpenAI API 호출 실패 - 상품 추천", e);
            throw new BusinessException(ErrorCode.OPENAI_API_CALL_FAILED,
                    "AI 상품 추천 생성에 실패했습니다: " + e.getMessage());
        }
    }

    private List<AIRecommendedProduct> parseAIResponseOfProduct(String content) {
        try {
            String cleanedContent = content
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            log.debug("파싱할 JSON: {}", cleanedContent);

            return objectMapper.readValue(
                    cleanedContent,
                    new TypeReference<List<AIRecommendedProduct>>() {
                    }
            );

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패 - content: {}", content, e);
            throw new BusinessException(ErrorCode.INVALID_JSON_RESPONSE,
                    "AI 응답을 파싱할 수 없습니다.");
        }
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
        format.append("- **relevanceScore**: 85~100 사이 점수\n");

        if (withProduct) {
            format.append("- **reason**: 타겟 상품 연관성(50%) + 고객 적합성(50%) 모두 명시\n");
        } else {
            format.append("- **reason**: 고객의 이름과 구체적 상황을 포함한 개인화된 설명\n");
        }

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

    private ProductRecommendationResponse.CustomerProfileSummary buildCustomerProfileSummaryForProduct(
            Customer customer) {
        Integer yearsAsCustomer = null;
        String joinDate = null;

        if (customer.getJoinDate() != null) {
            yearsAsCustomer = Math.toIntExact(ChronoUnit.YEARS.between(customer.getJoinDate(), LocalDateTime.now()));
            joinDate = customer.getJoinDate().toLocalDate().toString();
        }

        return ProductRecommendationResponse.CustomerProfileSummary.builder()
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

    private TargetCampaignInfo buildTargetCampaignInfo(Campaign campaign) {
        return TargetCampaignInfo.builder()
                .campaignId(campaign.getCampaignId())
                .campaignName(campaign.getName())
                .campaignType(campaign.getType().getDisplayName())
                .description(campaign.getDescription())
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

    private Campaign findCampaignById(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND,
                        "캠페인을 찾을 수 없습니다. campaignId: " + campaignId));
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
}