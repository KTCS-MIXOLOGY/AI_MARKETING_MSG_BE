package com.ai_marketing_msg_be.domain.message.entity;

import com.ai_marketing_msg_be.common.entity.BaseEntity;
import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "AI생성메시지")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "message_group_id", length = 50)
    private String messageGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private MessageType messageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id")
    private Segment segment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String messageContent;

    @Column(name = "message_version", nullable = false)
    private Integer messageVersion;

    @Column(name = "character_count")
    private Integer characterCount;

    @Column(name = "tone_id", length = 20)
    private String toneId;  // "TONE001", "TONE002" 등

    @Column(name = "ai_model_used", length = 50)
    private String aiModelUsed;

    @Column(name = "generation_prompt", columnDefinition = "TEXT")
    private String generationPrompt;

    @PrePersist
    protected void onPrePersist() {
        calculateCharacterCount();
        validate();
    }

    @PreUpdate
    protected void onPreUpdate() {
        calculateCharacterCount();
    }

    private void calculateCharacterCount() {
        if (this.messageContent != null) {
            this.characterCount = this.messageContent.length();
        }
    }

    private void validate() {
        if (messageType == MessageType.SEGMENT) {
            if (segment == null) {
                throw new BusinessException(ErrorCode.SEGMENT_REQUIRED);
            }
            if (customer != null) {
                throw new BusinessException(ErrorCode.CUSTOMER_NOT_ALLOWED);
            }
        } else if (messageType == MessageType.INDIVIDUAL) {
            if (customer == null) {
                throw new BusinessException(ErrorCode.CUSTOMER_REQUIRED);
            }
            if (segment != null) {
                throw new BusinessException(ErrorCode.SEGMENT_NOT_ALLOWED);
            }
        }
    }

    public void updateMessageContent(String newContent) {
        this.messageContent = newContent;
        this.characterCount = newContent.length();
    }

    public boolean isSegmentMessage() {
        return this.messageType == MessageType.SEGMENT;
    }

    public boolean isIndividualMessage() {
        return this.messageType == MessageType.INDIVIDUAL;
    }

    public ToneManner getToneManner() {
        if (this.toneId == null) {
            return null;
        }
        return ToneManner.fromToneId(this.toneId);
    }

    public String getToneName() {
        ToneManner tone = getToneManner();
        return tone != null ? tone.getToneName() : null;
    }

    public String getContentPreview() {
        if (this.messageContent == null) {
            return "";
        }
        int previewLength = Math.min(50, this.messageContent.length());
        String preview = this.messageContent.substring(0, previewLength);
        return this.messageContent.length() > 50 ? preview + "..." : preview;
    }

    public String getCampaignName() {
        return this.campaign != null ? this.campaign.getName() : null;
    }

    public String getProductName() {
        return this.product != null ? this.product.getName() : null;
    }

    public String getCustomerName() {
        if (isIndividualMessage() && this.customer != null) {
            return this.customer.getName();
        }
        return null;
    }

    public String getSegmentDescription() {
        if (isSegmentMessage() && this.segment != null) {
            return this.segment.getDescription();
        }
        return null;
    }

    public Integer getTargetCustomerCount() {
        if (isSegmentMessage() && this.segment != null) {
            return this.segment.getTargetCustomerCount();
        }
        return null;
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public static class MessageBuilder {
        private String messageGroupId;
        private User user;
        private Campaign campaign;
        private Product product;
        private MessageType messageType;
        private Segment segment;
        private Customer customer;
        private String messageContent;
        private Integer messageVersion;
        private String toneId;
        private String aiModelUsed;
        private String generationPrompt;

        public MessageBuilder messageGroupId(String messageGroupId) {
            this.messageGroupId = messageGroupId;
            return this;
        }

        public MessageBuilder user(User user) {
            this.user = user;
            return this;
        }

        public MessageBuilder campaign(Campaign campaign) {
            this.campaign = campaign;
            return this;
        }

        public MessageBuilder product(Product product) {
            this.product = product;
            return this;
        }

        public MessageBuilder messageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public MessageBuilder segment(Segment segment) {
            this.segment = segment;
            return this;
        }

        public MessageBuilder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public MessageBuilder messageContent(String messageContent) {
            this.messageContent = messageContent;
            return this;
        }

        public MessageBuilder messageVersion(Integer messageVersion) {
            this.messageVersion = messageVersion;
            return this;
        }

        public MessageBuilder toneId(String toneId) {
            this.toneId = toneId;
            return this;
        }

        public MessageBuilder aiModelUsed(String aiModelUsed) {
            this.aiModelUsed = aiModelUsed;
            return this;
        }

        public MessageBuilder generationPrompt(String generationPrompt) {
            this.generationPrompt = generationPrompt;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.messageGroupId = this.messageGroupId;
            message.user = this.user;
            message.campaign = this.campaign;
            message.product = this.product;
            message.messageType = this.messageType;
            message.segment = this.segment;
            message.customer = this.customer;
            message.messageContent = this.messageContent;
            message.messageVersion = this.messageVersion;
            message.toneId = this.toneId;
            message.aiModelUsed = this.aiModelUsed;
            message.generationPrompt = this.generationPrompt;
            return message;
        }
    }
}