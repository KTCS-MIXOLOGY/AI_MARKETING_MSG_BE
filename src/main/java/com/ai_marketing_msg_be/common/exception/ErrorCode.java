package com.ai_marketing_msg_be.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "Invalid input value"),
    RESOURCE_NOT_FOUND(404, "Resource not found"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),

    // Campaign
    CAMPAIGN_NOT_FOUND(404, "Campaign not found"),
    CAMPAIGN_ALREADY_EXISTS(409, "Campaign already exists"),
    CAMPAIGN_CANNOT_BE_DELETED(400, "Campaign cannot be deleted"),
    INVALID_CAMPAIGN_STATUS(400, "Invalid campaign status"),
    INVALID_CAMPAIGN_DATE(400, "Invalid campaign date range"),

    // Product
    PRODUCT_NOT_FOUND(404, "Product not found"),
    PRODUCT_ALREADY_EXISTS(409, "Product already exists"),
    PRODUCT_CANNOT_BE_DELETED(400, "Product cannot be deleted"),
    INVALID_PRODUCT_PRICE(400, "Invalid product price"),
    INVALID_DISCOUNT_RATE(400, "Invalid discount rate"),
    OUT_OF_STOCK(400, "Product is out of stock"),

    // CampaignProduct
    CAMPAIGN_PRODUCT_ALREADY_EXISTS(409, "Product is already added to this campaign"),
    CAMPAIGN_PRODUCT_NOT_FOUND(404, "Campaign-Product mapping not found"),

    // Auth
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    INVALID_TOKEN(401, "Invalid token"),
    TOKEN_EXPIRED(401, "Token expired"),
    INVALID_PASSWORD(401, "Invalid password"),
    USER_NOT_APPROVED(403, "User not approved"),
    USER_REJECTED(403, "User rejected"),

    // User
    USER_NOT_FOUND(404, "User not found"),
    DUPLICATE_USERNAME(400, "Username already exists"),
    DUPLICATE_EMAIL(409, "Email already exists"),
    USER_ALREADY_APPROVED(400, "User is already approved"),
    DUPLICATE_PHONE(409, "Phone number already exists"),
    INVALID_USER_ROLE(400, "Invalid user role"),
    INVALID_USER_STATUS(400, "Invalid user status"),
    USER_ALREADY_DELETED(400, "User is already deleted"),

    // Customer
    CUSTOMER_NOT_FOUND(404, "Customer not found"),
    INVALID_SEARCH_TYPE(400, "Invalid search type"),
    INVALID_SEGMENT_FILTER(400, "Invalid segment filter"),

    // Message 조회 관련
    MESSAGE_NOT_FOUND(404, "Message not found"),
    INVALID_MESSAGE_TYPE(400, "Invalid message type"),
    INVALID_MESSAGE_VERSION(400, "Invalid message version"),

    // Message 저장 시 제약 조건
    SEGMENT_REQUIRED(400, "Segment is required for SEGMENT type message"),
    CUSTOMER_REQUIRED(400, "Customer is required for INDIVIDUAL type message"),
    SEGMENT_NOT_ALLOWED(400, "Segment is not allowed for INDIVIDUAL type message"),
    CUSTOMER_NOT_ALLOWED(400, "Customer is not allowed for SEGMENT type message"),

    // Segment 관련
    SEGMENT_NOT_FOUND(404, "Segment not found"),

    // ToneManner 관련
    INVALID_TONE_ID(400, "Invalid tone ID"),
    TONE_NOT_FOUND(404, "Tone not found"),

    // MessageGroup 관련
    MESSAGE_GROUP_NOT_FOUND(404, "Message group not found"),
    INVALID_MESSAGE_GROUP_ID(400, "Invalid message group ID"),

    // AI 관련 에러 (기존 코드 하단에 추가)
    OPENAI_API_CALL_FAILED(500, "OpenAI API 호출에 실패했습니다."),
    OPENAI_API_TIMEOUT(408, "OpenAI API 요청 시간이 초과되었습니다."),
    INVALID_JSON_RESPONSE(500, "AI 응답 파싱에 실패했습니다."),
    MESSAGE_GENERATION_FAILED(500, "메시지 생성에 실패했습니다."),
    INVALID_PROMPT_CONTEXT(400, "프롬프트 생성에 필요한 정보가 부족합니다.");

    private final int status;
    private final String message;
}
