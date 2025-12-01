package com.ai_marketing_msg_be.domain.message.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.message.dto.GetMessageLogDetailResponse;
import com.ai_marketing_msg_be.domain.message.dto.GetMessageLogListResponse;
import com.ai_marketing_msg_be.domain.message.dto.MessageLogItem;
import com.ai_marketing_msg_be.domain.message.entity.Message;
import com.ai_marketing_msg_be.domain.message.repository.MessageRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public GetMessageLogListResponse getMessageList(int page, int size) {
        log.info("Fetching message list for admin: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<Message> messagePage = messageRepository.findAll(pageable);

        List<MessageLogItem> messages = messagePage.getContent().stream()
                .map(MessageLogItem::from)
                .collect(Collectors.toList());

        return GetMessageLogListResponse.of(
                messages,
                messagePage.getTotalElements(),
                page,
                messagePage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public GetMessageLogDetailResponse getMessageDetail(Long messageId) {
        log.info("Fetching message detail for admin: messageId={}", messageId);

        Message message = messageRepository.findByMessageIdWithDetails(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        User executor = userRepository.findById(message.getUser().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return GetMessageLogDetailResponse.from(message, executor);
    }

    @Transactional(readOnly = true)
    public GetMessageLogListResponse getMessagesByCampaign(Long campaignId, int page, int size) {
        log.info("Fetching messages by campaign for admin: campaignId={}, page={}, size={}",
                campaignId, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<Message> messagePage = messageRepository.findByCampaign_CampaignId(campaignId, pageable);

        List<MessageLogItem> messages = messagePage.getContent().stream()
                .map(MessageLogItem::from)
                .collect(Collectors.toList());

        return GetMessageLogListResponse.of(
                messages,
                messagePage.getTotalElements(),
                page,
                messagePage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public GetMessageLogListResponse getMessagesByUser(Long userId, int page, int size) {
        log.info("Fetching messages by user for admin: userId={}, page={}, size={}",
                userId, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<Message> messagePage = messageRepository.findByUserId(userId, pageable);

        List<MessageLogItem> messages = messagePage.getContent().stream()
                .map(MessageLogItem::from)
                .collect(Collectors.toList());

        return GetMessageLogListResponse.of(
                messages,
                messagePage.getTotalElements(),
                page,
                messagePage.getTotalPages()
        );
    }
}