package com.ai_marketing_msg_be.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private boolean success;
    private T data;
    private String timestamp;
    private String path;
    private String message;

    public static <T> ApiResponse<T> ok(T data, String path) {
        return ApiResponse.<T>builder()
                .status(200)
                .success(true)
                .data(data)
                .timestamp(getCurrentTimestamp())
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String path) {
        return ApiResponse.<T>builder()
                .status(201)
                .success(true)
                .data(data)
                .timestamp(getCurrentTimestamp())
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, String path) {
        return ApiResponse.<T>builder()
                .status(status)
                .success(false)
                .message(message)
                .timestamp(getCurrentTimestamp())
                .path(path)
                .build();
    }

    private static String getCurrentTimestamp() {
        return ZonedDateTime.now(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
