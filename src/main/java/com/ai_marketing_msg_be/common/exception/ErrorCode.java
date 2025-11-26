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
    USER_ALREADY_DELETED(400, "User is already deleted");
    
    private final int status;
    private final String message;
}
