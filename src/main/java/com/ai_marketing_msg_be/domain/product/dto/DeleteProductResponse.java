package com.ai_marketing_msg_be.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 삭제 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProductResponse {
    private Long productId;
    private boolean deleted;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime deletedAt;

    public static DeleteProductResponse of(Long productId) {
        return DeleteProductResponse.builder()
                .productId(productId)
                .deleted(true)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
