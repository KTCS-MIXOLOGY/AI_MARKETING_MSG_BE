package com.ai_marketing_msg_be.domain.messagelog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExecutorInfo {
    private Long userId;
    private String name;
    private String department;
}
