package com.ai_marketing_msg_be.domain.messagelog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TargetInfo {
    private String type;
    private String segmentName;
}
