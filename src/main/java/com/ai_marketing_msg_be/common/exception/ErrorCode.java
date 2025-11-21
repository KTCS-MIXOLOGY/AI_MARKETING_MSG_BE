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

    // Auth
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    INVALID_TOKEN(401, "Invalid token");

    private final int status;
    private final String message;
}
