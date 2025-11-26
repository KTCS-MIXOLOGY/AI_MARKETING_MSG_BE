package com.ai_marketing_msg_be.domain.messagelog.entity;

import com.ai_marketing_msg_be.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "메시지_로그")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "segment_id")
    private Long segmentId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String messageContent;

    @Column(name = "message_version", nullable = false)
    private Integer messageVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "character_count", nullable = false)
    private Integer characterCount;

    @Column(name = "ai_model_used", length = 50)
    private String aiModelUsed;

    @Column(name = "generation_prompt", columnDefinition = "TEXT")
    private String generationPrompt;

    @Column(name = "tone", length = 50)
    private String tone;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "segment_name", length = 100)
    private String segmentName;

    @Builder
    public MessageLog(
            Long campaignId,
            Long segmentId,
            Long customerId,
            String messageContent,
            Integer messageVersion,
            MessageType messageType,
            Integer characterCount,
            String aiModelUsed,
            String generationPrompt,
            String tone,
            Long createdBy,
            TargetType targetType,
            String segmentName
    ) {
        this.campaignId = campaignId;
        this.segmentId = segmentId;
        this.customerId = customerId;
        this.messageContent = messageContent;
        this.messageVersion = messageVersion != null ? messageVersion : 1;
        this.messageType = messageType;
        this.characterCount = characterCount;
        this.aiModelUsed = aiModelUsed;
        this.generationPrompt = generationPrompt;
        this.tone = tone;
        this.createdBy = createdBy;
        this.targetType = targetType;
        this.segmentName = segmentName;
    }

    public String getSummary() {
        if (messageContent == null || messageContent.isEmpty()) {
            return "";
        }
        int maxLength = 50;
        return messageContent.length() > maxLength
            ? messageContent.substring(0, maxLength) + "..."
            : messageContent;
    }
}
