package com.ai_marketing_msg_be.domain.message.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.repository.CampaignRepository;
import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.ai_marketing_msg_be.domain.customer.repository.CustomerRepository;
import com.ai_marketing_msg_be.domain.message.dto.GetMessageListResponse;
import com.ai_marketing_msg_be.domain.message.dto.MessageDetailResponse;
import com.ai_marketing_msg_be.domain.message.dto.MessageListItemResponse;
import com.ai_marketing_msg_be.domain.message.dto.SaveMessageRequest;
import com.ai_marketing_msg_be.domain.message.dto.SaveMessageResponse;
import com.ai_marketing_msg_be.domain.message.entity.Message;
import com.ai_marketing_msg_be.domain.message.entity.MessageType;
import com.ai_marketing_msg_be.domain.message.entity.Segment;
import com.ai_marketing_msg_be.domain.message.repository.MessageRepository;
import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.product.repository.ProductRepository;
import com.ai_marketing_msg_be.domain.user.entity.User;
import com.ai_marketing_msg_be.domain.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final SegmentService segmentService;
    private final CampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Transactional
    public SaveMessageResponse saveMessage(SaveMessageRequest request) {
        log.info("Saving message: type={}, groupId={}", request.getMessageType());

        User currentUser = getCurrentUser();

        Campaign campaign = getCampaign(request.getCampaignId());
        Product product = getProduct(request.getProductId());

        Message.MessageBuilder builder = Message.builder()
                .user(currentUser)
                .campaign(campaign)
                .product(product)
                .messageContent(request.getMessageContent())
                .messageVersion(request.getMessageVersion())
                .toneId(request.getToneId())
                .aiModelUsed(request.getAiModelUsed())
                .generationPrompt(request.getGenerationPrompt());

        MessageType messageType;
        try {
            messageType = MessageType.valueOf(request.getMessageType().toUpperCase());
            builder.messageType(messageType);
        } catch (IllegalArgumentException e) {
            log.error("Invalid message type: {}", request.getMessageType());
            throw new BusinessException(ErrorCode.INVALID_MESSAGE_TYPE);
        }

        if (messageType == MessageType.SEGMENT) {
            if (request.getSegmentFilter() == null) {
                throw new BusinessException(ErrorCode.INVALID_SEGMENT_FILTER);
            }
            Segment segment = segmentService.findOrCreateSegment(request.getSegmentFilter());
            builder.segment(segment);
            log.info("Segment assigned: segmentId={}", segment.getSegmentId());
        } else if (messageType == MessageType.INDIVIDUAL) {
            if (request.getCustomerId() == null) {
                throw new BusinessException(ErrorCode.CUSTOMER_REQUIRED);
            }
            Customer customer = getCustomer(request.getCustomerId());
            builder.customer(customer);
            log.info("Customer assigned: customerId={}", customer.getCustomerId());
        }

        Message message = messageRepository.save(builder.build());
        log.info("Message saved: messageId={}, characterCount={}",
                message.getMessageId(), message.getCharacterCount());

        return SaveMessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public GetMessageListResponse getMessages(int page, int size) {
        log.info("Getting messages: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<Message> messagePage = messageRepository.findAll(pageable);

        List<MessageListItemResponse> items = messagePage.getContent().stream()
                .map(MessageListItemResponse::from)
                .collect(Collectors.toList());

        log.info("Messages retrieved: {} messages, totalCount={}",
                items.size(), messagePage.getTotalElements());

        return GetMessageListResponse.of(
                items,
                messagePage.getTotalElements(),
                page,
                messagePage.getTotalPages()
        );
    }


    @Transactional(readOnly = true)
    public MessageDetailResponse getMessageDetail(Long messageId) {
        log.info("Getting message detail: messageId={}", messageId);

        Message message = messageRepository.findByMessageIdWithDetails(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        log.info("Message detail retrieved: messageId={}, type={}",
                messageId, message.getMessageType());

        return MessageDetailResponse.from(message);
    }


    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Campaign getCampaign(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Customer getCustomer(Long customerId) {
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }
}