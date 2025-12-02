package com.ai_marketing_msg_be.domain.message.service;

import com.ai_marketing_msg_be.domain.customer.dto.SegmentFilterRequest;
import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.ai_marketing_msg_be.domain.message.vo.PromptContext;
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

        if (filter.getMembershipLevel() != null) {
            info.append(String.format("- 멤버십: %s 등급\n", filter.getMembershipLevel()));
        }

        if (filter.getRecencyMaxDays() != null) {
            info.append(String.format("- 최근 구매: %d일 이내\n", filter.getRecencyMaxDays()));
        }

        return info.toString();
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

        info.append(String.format("- 상품명: %s\n", context.getProduct().getName()));
        info.append(String.format("- 카테고리: %s\n", context.getProduct().getCategory()));

        if (context.getProduct().getPrice() != null) {
            info.append(String.format("- 가격: %,d원\n", context.getProduct().getPrice().intValue()));
        }

        if (context.getProduct().getDiscountRate() != null &&
                context.getProduct().getDiscountRate().intValue() > 0) {
            info.append(String.format("- 할인율: %d%%\n", context.getProduct().getDiscountRate().intValue()));
        }

        if (context.getProduct().getBenefits() != null) {
            info.append(String.format("- 주요 혜택: %s\n", context.getProduct().getBenefits()));
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

        req.append("위 정보를 바탕으로 SMS/알림톡용 마케팅 메시지 3가지 버전을 생성해주세요.\n\n");
        req.append("각 메시지는:\n");
        req.append("1. 90-120자 이내\n");
        req.append("2. 명확한 행동 유도(CTA) 포함\n");
        req.append("3. 이모지 적절히 활용\n");
        req.append("4. 버전별로 톤이나 강조점이 약간씩 다르게\n\n");
        req.append("JSON 형식으로만 응답해주세요:\n");
        req.append("[\n");
        req.append("  {\"version\": 1, \"content\": \"메시지 내용\"},\n");
        req.append("  {\"version\": 2, \"content\": \"메시지 내용\"},\n");
        req.append("  {\"version\": 3, \"content\": \"메시지 내용\"}\n");
        req.append("]\n");

        return req.toString();
    }
}