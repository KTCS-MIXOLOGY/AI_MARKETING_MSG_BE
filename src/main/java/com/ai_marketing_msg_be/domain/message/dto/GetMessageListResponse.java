package com.ai_marketing_msg_be.domain.message.dto;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetMessageListResponse {

    private List<MessageListItemResponse> messages;
    private Long totalCount;
    private Integer currentPage;
    private Integer totalPages;

    public static GetMessageListResponse of(
            List<MessageListItemResponse> messages,
            Long totalCount,
            Integer currentPage,
            Integer totalPages) {
        return GetMessageListResponse.builder()
                .messages(messages)
                .totalCount(totalCount)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .build();
    }
}












