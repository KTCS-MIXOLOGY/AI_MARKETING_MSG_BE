package com.ai_marketing_msg_be.domain.product.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 재고 상태
 */
@Getter
@RequiredArgsConstructor
public enum StockStatus {
    IN_STOCK("IN_STOCK", "재고 있음"),
    OUT_OF_STOCK("OUT_OF_STOCK", "품절"),
    LIMITED("LIMITED", "한정 수량");

    private final String code;
    private final String displayName;

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static StockStatus from(String value) {
        for (StockStatus status : StockStatus.values()) {
            if (status.code.equals(value) || status.name().equals(value) || status.displayName.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid stock status: " + value);
    }
}
