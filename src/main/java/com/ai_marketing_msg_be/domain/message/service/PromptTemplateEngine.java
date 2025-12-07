package com.ai_marketing_msg_be.domain.message.service;

import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.customer.dto.SegmentFilterRequest;
import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.ai_marketing_msg_be.domain.message.vo.PromptContext;
import com.ai_marketing_msg_be.domain.product.entity.Product;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PromptTemplateEngine {


    public String buildPrompt(PromptContext context) {
        if (context.isSegmentContext()) {
            return buildSegmentPrompt(context);
        } else if (context.isIndividualContext()) {
            return buildIndividualPrompt(context);
        } else {
            throw new IllegalArgumentException("Invalid prompt context: neither segment nor individual");
        }
    }

    private String buildSegmentPrompt(PromptContext context) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 KT의 전문 마케팅 메시지 작성자입니다.\n");
        prompt.append("고객 세그먼트 데이터를 분석하여 개인화된 SMS/알림톡 메시지를 생성합니다.\n\n");

        prompt.append("[타겟 세그먼트]\n");
        prompt.append(buildSegmentInfo(context.getSegmentFilter()));
        prompt.append(String.format("- 타겟 고객 수: %,d명\n\n", context.getTargetCustomerCount()));

        prompt.append(buildProductInfo(context));
        prompt.append(buildCampaignInfo(context));
        prompt.append(buildToneInfo(context));

        if (context.getAdditionalContext() != null && !context.getAdditionalContext().isEmpty()) {
            prompt.append("[추가 컨텍스트]\n");
            prompt.append(context.getAdditionalContext()).append("\n\n");
        }

        prompt.append(buildGenerationRequirements());

        log.debug("Generated segment prompt: {}", prompt);
        return prompt.toString();
    }

    private String buildIndividualPrompt(PromptContext context) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 KT의 1:1 개인화 마케팅 전문가입니다.\n");
        prompt.append("고객의 프로필과 구매 이력을 분석하여 맞춤형 메시지를 생성합니다.\n\n");

        prompt.append("[고객 프로필]\n");
        prompt.append(buildCustomerInfo(context.getCustomer()));
        prompt.append("\n");

        prompt.append("[캠페인 정보]\n");
        prompt.append(String.format("- 캠페인명: %s\n", context.getCampaign().getName()));
        prompt.append(String.format("- 캠페인 유형: %s\n", context.getCampaign().getType().getDisplayName()));
        if (context.getCampaign().getDescription() != null) {
            prompt.append(String.format("- 캠페인 목적: %s\n", context.getCampaign().getDescription()));
        }
        prompt.append("\n");

        prompt.append(buildProductInfo(context));

        prompt.append(buildToneInfo(context));

        if (context.getAdditionalContext() != null && !context.getAdditionalContext().isEmpty()) {
            prompt.append("[추가 컨텍스트]\n");
            prompt.append(context.getAdditionalContext()).append("\n\n");
        }

        prompt.append("**중요**: 고객의 이름과 프로필 정보를 자연스럽게 활용하여 개인화된 메시지를 작성하세요.\n\n");

        prompt.append(buildGenerationRequirements());

        log.debug("Generated individual prompt: {}", prompt);
        return prompt.toString();
    }


    private String buildSegmentInfo(SegmentFilterRequest filter) {
        StringBuilder info = new StringBuilder();

        if (filter.getAgeRange() != null) {
            info.append(String.format("- 연령대: %d~%d세\n",
                    filter.getAgeRange().getMin(),
                    filter.getAgeRange().getMax()));
        }

        if (filter.getGender() != null) {
            String genderKr = filter.getGender().equals("MALE") ? "남성" : "여성";
            info.append(String.format("- 성별: %s\n", genderKr));
        }

        if (filter.getRegions() != null && !filter.getRegions().isEmpty()) {
            info.append(String.format("- 지역: %s\n", String.join(", ", filter.getRegions())));
        }

        if (filter.getMembershipLevel() != null && !filter.getMembershipLevel().trim().isEmpty()) {
            String membershipKr = getMembershipDescription(filter.getMembershipLevel());
            info.append(String.format("- 멤버십: %s 등급\n", membershipKr));
        } else {
            info.append("- 멤버십: 전체 등급 (등급 제한 없음)\n");
        }

        if (filter.getRecencyMaxDays() != null) {
            info.append(String.format("- 최근 구매: %d일 이내\n", filter.getRecencyMaxDays()));
        }

        return info.toString();
    }

    private String getMembershipDescription(String membershipLevel) {
        switch (membershipLevel) {
            case "BASIC":
                return "일반";
            case "WHITE":
                return "화이트";
            case "SILVER":
                return "실버";
            case "GOLD":
                return "골드";
            case "VIP":
                return "VIP";
            case "VVIP":
                return "VVIP";
            default:
                return membershipLevel;
        }
    }

    private String buildCustomerInfo(Customer customer) {
        StringBuilder info = new StringBuilder();

        info.append(String.format("- 이름: %s\n", customer.getName()));
        info.append(String.format("- 연령: %d세\n", customer.getAge()));
        info.append(String.format("- 성별: %s\n",
                customer.getGender() != null ? customer.getGender().getDescription() : "미지정"));
        info.append(String.format("- 지역: %s\n",
                customer.getRegion() != null ? customer.getRegion().getDescription() : "미지정"));
        info.append(String.format("- 멤버십: %s\n",
                customer.getMembershipLevel() != null ? customer.getMembershipLevel().getDescription() : "미지정"));

        if (customer.getCurrentPlan() != null) {
            info.append(String.format("- 현재 요금제: %s\n", customer.getCurrentPlan()));
        }
        if (customer.getCurrentDevice() != null) {
            info.append(String.format("- 현재 단말기: %s\n", customer.getCurrentDevice()));
        }

        if (customer.getRecencyDays() != null) {
            info.append(String.format("- 최근 구매: %d일 전\n", customer.getRecencyDays()));
        }

        return info.toString();
    }


    private String buildProductInfo(PromptContext context) {
        StringBuilder info = new StringBuilder();
        info.append("[상품 정보]\n");

        Product product = context.getProduct();

        info.append(String.format("- 상품명: %s\n", product.getName()));
        info.append(String.format("- 카테고리: %s\n", product.getCategory()));

        if (product.getPrice() != null) {
            info.append(String.format("**정상 가격**: %,d원\n", product.getPrice().intValue()));

            if (product.getDiscountRate() != null && product.getDiscountRate().intValue() > 0) {
                BigDecimal discountedPrice = product.getDiscountedPrice();
                info.append(String.format("**할인율**: %d%% 할인\n", product.getDiscountRate().intValue()));
                info.append(String.format("**할인가**: %,d원\n", discountedPrice.intValue()));
            }
        }

        if (product.getBenefits() != null && !product.getBenefits().isEmpty()) {
            info.append("\n**📌 주요 혜택 (메시지에 반드시 포함할 것)**:\n");

            String[] benefitsList = product.getBenefits().split("/");
            for (String benefit : benefitsList) {
                info.append(String.format("  • %s\n", benefit.trim()));
            }
        }

        info.append("\n⚠️ **중요**: 위 혜택 중 최소 2~3가지는 메시지에 구체적으로 포함해주세요.\n");
        info.append("\n");
        return info.toString();
    }

    private String buildCampaignInfo(PromptContext context) {
        StringBuilder info = new StringBuilder();
        Campaign campaign = context.getCampaign();

        info.append("[진행 중인 마케팅 캠페인 정보]\n");
        info.append(String.format("- 캠페인명: %s\n", campaign.getName()));
        info.append(String.format("- 캠페인 유형: %s\n", campaign.getType().getDisplayName()));

        if (campaign.getDescription() != null && !campaign.getDescription().isEmpty()) {
            info.append("\n🎁 **캠페인 특별 혜택 (메시지에 반드시 1개 이상 포함)**:\n");

            String[] benefits = campaign.getDescription().split("[,.]");
            int count = 1;
            for (String benefit : benefits) {
                String trimmed = benefit.trim();
                if (!trimmed.isEmpty()) {
                    info.append(String.format("  %d. %s\n", count++, trimmed));
                }
            }
        }

        if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
            info.append(String.format("\n- 캠페인 기간: %s ~ %s\n",
                    campaign.getStartDate(), campaign.getEndDate()));
        }

        info.append("\n");
        return info.toString();
    }

    private String buildToneInfo(PromptContext context) {
        StringBuilder info = new StringBuilder();
        info.append("[톤앤매너]\n");

        if (context.getToneManner() != null) {
            info.append(String.format("- 스타일: %s\n", context.getToneManner().getToneName()));
            info.append(String.format("- 설명: %s\n", context.getToneManner().getDescription()));
            info.append(String.format("- 예시: %s\n", context.getToneManner().getExample()));
        } else {
            info.append("- 스타일: 자연스럽고 친근한 톤\n");
        }

        info.append("\n");
        return info.toString();
    }

    private String buildGenerationRequirements() {
        StringBuilder req = new StringBuilder();

        req.append("📝 **메시지 생성 요구사항**:\n\n");
        req.append("위 정보를 바탕으로 SMS/알림톡용 마케팅 메시지 3가지 버전을 생성해주세요.\n\n");
        req.append("각 메시지는 다음을 반드시 포함해야 합니다:\n");
        req.append("1. **캠페인의 특별 혜택** 1~2가지 (위 '캠페인 특별 혜택'에서 선택)\n");
        req.append("2. **상품의 핵심 혜택** 1~2가지 (위 '상품 핵심 혜택'에서 선택)\n");
        req.append("3. **가격/할인 정보** (있는 경우)\n");
        req.append("4. **타겟 고객에 대한 호칭** (예: VIP 고객님, 20대 여성 고객님)\n");
        req.append("5. **명확한 행동 유도(CTA)**\n");
        req.append("6. 이모지를 적절히 활용하여 시각적 효과 극대화\n\n");

        req.append("**글자 수**: 90-120자 이내\n\n");

        req.append("❌ **피해야 할 것**: \n");
        req.append("  - 캠페인 설명만 나열하거나, 상품 설명만 나열하지 마세요!\n");
        req.append("  - 타겟 세그먼트에 명시되지 않은 멤버십 등급으로 호칭하지 마세요!\n");
        req.append("  - '전체 등급'일 때 특정 등급(골드, VIP 등)을 임의로 선택하지 마세요!\n\n");
        req.append("✅ **해야 할 것**: \n");
        req.append("  - 캠페인 특별 혜택 + 상품 핵심 혜택을 조합하여 매력적으로 전달하세요!\n");
        req.append("  - [타겟 세그먼트]에 명시된 멤버십 등급을 정확히 사용하세요!\n");
        req.append("  - 멤버십이 '전체 등급'이면 '고객님' 또는 '연령대 기반 호칭'을 사용하세요!\n\n");

        req.append("JSON 형식으로만 응답해주세요:\n");
        req.append("[\n");
        req.append("  {\"version\": 1, \"content\": \"메시지 내용\"},\n");
        req.append("  {\"version\": 2, \"content\": \"메시지 내용\"},\n");
        req.append("  {\"version\": 3, \"content\": \"메시지 내용\"}\n");
        req.append("]\n");

        return req.toString();
    }
}