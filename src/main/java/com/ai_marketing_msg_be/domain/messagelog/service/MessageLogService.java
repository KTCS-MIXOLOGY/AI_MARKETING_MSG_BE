package com.ai_marketing_msg_be.domain.messagelog.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.messagelog.dto.MessageLogDetailResponse;
import com.ai_marketing_msg_be.domain.messagelog.dto.MessageLogListResponse;
import com.ai_marketing_msg_be.domain.messagelog.entity.MessageLog;
import com.ai_marketing_msg_be.domain.messagelog.repository.MessageLogRepository;
import com.ai_marketing_msg_be.domain.user.entity.User;
import com.ai_marketing_msg_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;
    private final UserRepository userRepository;

    /**
     * 메시지 로그 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<MessageLogListResponse> getMessageLogs(Pageable pageable) {
        log.debug("Fetching message logs with page: {}, size: {}",
            pageable.getPageNumber(), pageable.getPageSize());

        Page<MessageLog> messageLogs = messageLogRepository.findAllMessages(pageable);

        log.info("Found {} message logs", messageLogs.getTotalElements());

        return messageLogs.map(MessageLogListResponse::from);
    }

    /**
     * 메시지 로그 상세 조회
     */
    @Transactional(readOnly = true)
    public MessageLogDetailResponse getMessageLogDetail(Long messageId) {
        log.debug("Fetching message log detail for messageId: {}", messageId);

        MessageLog messageLog = messageLogRepository.findByMessageId(messageId)
                .orElseThrow(() -> {
                    log.warn("Message log not found: messageId={}", messageId);
                    return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        User user = userRepository.findById(messageLog.getCreatedBy())
                .orElseThrow(() -> {
                    log.warn("User not found: userId={}", messageLog.getCreatedBy());
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        log.info("Message log detail retrieved successfully: messageId={}", messageId);

        return MessageLogDetailResponse.from(messageLog, user);
    }

    /**
     * 캠페인별 메시지 로그 조회
     */
    @Transactional(readOnly = true)
    public Page<MessageLogListResponse> getMessageLogsByCampaign(Long campaignId, Pageable pageable) {
        log.debug("Fetching message logs for campaignId: {}", campaignId);

        Page<MessageLog> messageLogs = messageLogRepository.findByCampaignId(campaignId, pageable);

        log.info("Found {} message logs for campaignId: {}",
            messageLogs.getTotalElements(), campaignId);

        return messageLogs.map(MessageLogListResponse::from);
    }

    /**
     * 사용자별 메시지 로그 조회
     */
    @Transactional(readOnly = true)
    public Page<MessageLogListResponse> getMessageLogsByUser(Long userId, Pageable pageable) {
        log.debug("Fetching message logs for userId: {}", userId);

        Page<MessageLog> messageLogs = messageLogRepository.findByCreatedBy(userId, pageable);

        log.info("Found {} message logs for userId: {}",
            messageLogs.getTotalElements(), userId);

        return messageLogs.map(MessageLogListResponse::from);
    }
}
