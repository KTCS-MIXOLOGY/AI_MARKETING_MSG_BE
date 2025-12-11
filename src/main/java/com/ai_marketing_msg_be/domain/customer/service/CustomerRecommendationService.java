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

        prompt.append("🚨 **절대 준수 규칙 - 위반 시 추천 무효** 🚨\n\n");

        prompt.append("❌ 규칙0: **반드시 정확히 3개 추천** (1개나 2개는 불가)\n");

        if (customer.getCurrentPlan() != null) {
            String currentPlan = customer.getCurrentPlan();
            if (currentPlan.contains("5G")) {
                prompt.append(String.format("❌ 규칙1: 고객은 '%s' 사용 중 → **LTE/3G 추천 절대 금지**\n", currentPlan));
            } else if (currentPlan.contains("LTE")) {
                prompt.append(String.format("❌ 규칙1: 고객은 '%s' 사용 중 → **3G 추천 절대 금지**\n", currentPlan));
            }
        }

        if (customer.getAvgDataUsageGb() != null) {
            BigDecimal dataUsage = customer.getAvgDataUsageGb();
            prompt.append(String.format("❌ 규칙2: 고객 데이터 %.1fGB/월 → **%.1fGB 미만 요금제 추천 금지**\n",
                    dataUsage, dataUsage));
            prompt.append("   예: 36.8GB 고객에게 15GB 요금제 추천 불가!\n");
        }

        if (customer.getMembershipLevel() != null) {
            String membership = customer.getMembershipLevel().name();
            if (membership.equals("WHITE") || membership.equals("BASIC")) {
                prompt.append("❌ 규칙3: WHITE/BASIC → **현재 가격 ±20% 초과 금지**\n");
                prompt.append("   예: 5만원 사용 중 → 4~6만원대만 OK, 9만원 절대 불가!\n");
            } else if (membership.equals("SILVER") || membership.equals("GOLD")) {
                prompt.append("❌ 규칙3: SILVER/GOLD → **현재 가격 ±30% 초과 지양**\n");
            } else if (membership.contains("VIP")) {
                prompt.append("✅ 규칙3: VIP/VVIP → 가격 제한 없음 (프리미엄 OK)\n");
            }
        }

        prompt.append("\n🔥 위 4가지 규칙 위반 = 즉시 제외! 🔥\n");
        prompt.append("=".repeat(60) + "\n\n");

        prompt.append("## 고객\n");
        prompt.append(String.format("%s / %d세 / %s\n",
                customer.getName(),
                customer.getAge(),
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "일반"));
        prompt.append(String.format("현재: %s", customer.getCurrentPlan()));
        if (customer.getAvgDataUsageGb() != null) {
            prompt.append(String.format(" / %.1fGB 사용", customer.getAvgDataUsageGb()));
        }
        prompt.append("\n\n");

        prompt.append("## 상품 목록\n");
        for (Product p : products) {
            String priceStr = p.getPrice() != null ? String.format("%,d원", p.getPrice().intValue()) : "가격 미정";
            prompt.append(String.format("[%d] %s | %s | %s\n",
                    p.getProductId(),
                    p.getName(),
                    priceStr,
                    p.getCategory()));
        }
        prompt.append("\n");

        prompt.append("## 추천 전 체크리스트\n");
        prompt.append("[ ] 다운그레이드 아닌가?\n");
        prompt.append("[ ] 데이터 사용량 충분한가?\n");
        prompt.append("[ ] 멤버십 가격대 맞는가?\n");
        prompt.append("\n⚠️ 모바일 요금제가 부족하면 OTT/디바이스/생활편의 카테고리 추천\n\n");

        prompt.append("## 응답 (JSON만, 다른 텍스트 금지)\n");
        prompt.append("⚠️ **반드시 정확히 3개 추천 필수** (더 많거나 적으면 안됨)\n\n");

        prompt.append("### ✍️ reason 작성 규칙 (매우 중요)\n");
        prompt.append("❌ 나쁜 예: \"적합하여 추천드립니다\" (너무 짧고 성의없음)\n");
        prompt.append("❌ 나쁜 예: \"김다혜, 27세, 5G 스탠다드 사용 중\" (단어만 나열)\n");
        prompt.append(
                "✅ 좋은 예: \"김다혜님은 27세 WHITE 등급으로 5G 스탠다드 요금제를 사용 중이며 월 36.8GB의 데이터를 사용합니다. 이 상품은 데이터 무제한과 OTT 혜택을 제공하여, 고객의 높은 데이터 사용 패턴과 멤버십 등급을 고려할 때 실질적인 비용 절감과 편의성 향상을 제공합니다.\"\n\n");

        prompt.append("**reason 필수 포함 (3가지 모두):**\n");
        prompt.append("1️⃣ 고객 상황: 이름 + 나이 + 등급 + 현재 요금제 + 데이터 사용량\n");
        prompt.append("2️⃣ 상품 특징: 이 상품만의 구체적인 장점/혜택\n");
        prompt.append("3️⃣ 연결고리: 왜 이 고객에게 이 상품이 맞는지 논리적 설명\n");
        prompt.append("**최소 길이: 2-3문장, 100자 이상**\n\n");

        prompt.append("[\n");
        prompt.append(
                "  {\"rank\":1, \"productId\":ID, \"reason\":\"구체적이고 상세한 2-3문장\", \"expectedBenefit\":\"혜택\", \"relevanceScore\":85-100},\n");
        prompt.append(
                "  {\"rank\":2, \"productId\":ID, \"reason\":\"구체적이고 상세한 2-3문장\", \"expectedBenefit\":\"혜택\", \"relevanceScore\":85-100},\n");
        prompt.append(
                "  {\"rank\":3, \"productId\":ID, \"reason\":\"구체적이고 상세한 2-3문장\", \"expectedBenefit\":\"혜택\", \"relevanceScore\":85-100}\n");
        prompt.append("]\n");

        return prompt.toString();
    }

    private String buildProductRecommendationPromptWithCampaign(
            Customer customer, List<Product> products, Campaign campaign) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("🚨 **절대 준수 규칙 - 위반 시 추천 무효** 🚨\n\n");

        prompt.append("❌ 규칙0: **반드시 정확히 3개 추천** (1개나 2개는 불가)\n");

        if (customer.getCurrentPlan() != null) {
            String currentPlan = customer.getCurrentPlan();
            if (currentPlan.contains("5G")) {
                prompt.append(String.format("❌ 규칙1: 고객은 '%s' 사용 중 → **LTE/3G 추천 절대 금지**\n", currentPlan));
            } else if (currentPlan.contains("LTE")) {
                prompt.append(String.format("❌ 규칙1: 고객은 '%s' 사용 중 → **3G 추천 절대 금지**\n", currentPlan));
            }
        }

        if (customer.getAvgDataUsageGb() != null) {
            BigDecimal dataUsage = customer.getAvgDataUsageGb();
            prompt.append(String.format("❌ 규칙2: 고객 데이터 %.1fGB/월 → **%.1fGB 미만 요금제 추천 금지**\n",
                    dataUsage, dataUsage));
        }

        if (customer.getMembershipLevel() != null) {
            String membership = customer.getMembershipLevel().name();
            if (membership.equals("WHITE") || membership.equals("BASIC")) {
                prompt.append("❌ 규칙3: WHITE/BASIC → **현재 가격 ±20% 초과 금지**\n");
            } else if (membership.equals("SILVER") || membership.equals("GOLD")) {
                prompt.append("❌ 규칙3: SILVER/GOLD → **현재 가격 ±30% 초과 지양**\n");
            } else if (membership.contains("VIP")) {
                prompt.append("✅ 규칙3: VIP/VVIP → 가격 제한 없음\n");
            }
        }

        prompt.append("\n🔥 위 4가지 규칙 위반 = 즉시 제외! 🔥\n");
        prompt.append("=".repeat(60) + "\n\n");

        prompt.append("## 타겟 캠페인\n");
        prompt.append(String.format("%s (%s)\n", campaign.getName(), campaign.getType().getDisplayName()));
        if (campaign.getDescription() != null) {
            prompt.append(String.format("혜택: %s\n", campaign.getDescription()));
        }
        prompt.append("\n");

        prompt.append("## 고객\n");
        prompt.append(String.format("%s / %d세 / %s\n",
                customer.getName(),
                customer.getAge(),
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "일반"));
        prompt.append(String.format("현재: %s", customer.getCurrentPlan()));
        if (customer.getAvgDataUsageGb() != null) {
            prompt.append(String.format(" / %.1fGB 사용", customer.getAvgDataUsageGb()));
        }
        prompt.append("\n\n");

        prompt.append("## 상품 목록\n");
        for (Product p : products) {
            String priceStr = p.getPrice() != null ? String.format("%,d원", p.getPrice().intValue()) : "가격 미정";
            prompt.append(String.format("[%d] %s | %s | %s\n",
                    p.getProductId(),
                    p.getName(),
                    priceStr,
                    p.getCategory()));
        }
        prompt.append("\n");

        prompt.append("## 추천 원칙\n");
        prompt.append("캠페인 목적 50% + 고객 적합성 50%\n\n");

        prompt.append("## 추천 전 체크리스트\n");
        prompt.append("[ ] 캠페인 목적에 맞는가?\n");
        prompt.append("[ ] 다운그레이드 아닌가?\n");
        prompt.append("[ ] 데이터 사용량 충분한가?\n");
        prompt.append("[ ] 멤버십 가격대 맞는가?\n");
        prompt.append("\n⚠️ 모바일 요금제가 부족하면 OTT/디바이스/생활편의 카테고리 추천\n\n");

        prompt.append("## 응답 (JSON만, 다른 텍스트 금지)\n");
        prompt.append("⚠️ **반드시 정확히 3개 추천 필수** (더 많거나 적으면 안됨)\n\n");

        prompt.append("### ✍️ reason 작성 규칙 (매우 중요)\n");
        prompt.append("❌ 나쁜 예: \"인터넷 속도 업그레이드 특별 할인, 김다혜, 5G 스탠다드 사용 중\" (단어만 나열)\n");
        prompt.append("❌ 나쁜 예: \"캠페인 혜택이 좋아서 추천\" (너무 짧고 성의없음)\n");
        prompt.append(
                "✅ 좋은 예: \"'인터넷 속도 업그레이드 특별 할인' 캠페인은 김다혜님(27세, WHITE 등급, 5G 스탠다드 사용 중)에게 적합합니다. 이 상품은 [구체적 상품 특징]을 제공하며, 캠페인의 [구체적 할인/혜택]과 결합하여 고객의 [니즈/상황]에 최적화된 솔루션을 제공합니다.\"\n\n");

        prompt.append("**reason 필수 포함 (4가지 모두):**\n");
        prompt.append("1️⃣ 캠페인명: 정확한 캠페인 이름\n");
        prompt.append("2️⃣ 고객 상황: 이름 + 나이 + 등급 + 현재 요금제\n");
        prompt.append("3️⃣ 상품 특징: 이 상품의 구체적 장점\n");
        prompt.append("4️⃣ 시너지 설명: 캠페인 혜택 + 상품 특징이 고객에게 주는 가치\n");
        prompt.append("**최소 길이: 2-3문장, 100자 이상**\n\n");

        prompt.append("[\n");
        prompt.append(
                "  {\"rank\":1, \"productId\":ID, \"reason\":\"구체적이고 상세한 2-3문장\", \"expectedBenefit\":\"혜택\", \"relevanceScore\":85-100},\n");
        prompt.append(
                "  {\"rank\":2, \"productId\":ID, \"reason\":\"구체적이고 상세한 2-3문장\", \"expectedBenefit\":\"혜택\", \"relevanceScore\":85-100},\n");
        prompt.append(
                "  {\"rank\":3, \"productId\":ID, \"reason\":\"구체적이고 상세한 2-3문장\", \"expectedBenefit\":\"혜택\", \"relevanceScore\":85-100}\n");
        prompt.append("]\n");

        return prompt.toString();
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
                    .temperature(0.3)
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
                    .temperature(0.3)
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