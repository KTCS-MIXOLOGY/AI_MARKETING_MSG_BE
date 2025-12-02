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
public class GetMessageLogListResponse {

    private List<MessageLogItem> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    public static GetMessageLogListResponse of(
            List<MessageLogItem> content,
            Long totalElements,
            Integer page,
            Integer totalPages) {
        return GetMessageLogListResponse.builder()
                .content(content)
                .page(page)
                .size(content.size())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}