package com.ai_marketing_msg_be.domain.messagelog.repository;

import com.ai_marketing_msg_be.domain.messagelog.entity.MessageLog;
import com.ai_marketing_msg_be.domain.messagelog.entity.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {

    /**
     * 메시지 로그 목록 조회 (페이징)
     */
    @Query("SELECT m FROM MessageLog m ORDER BY m.createdAt DESC")
    Page<MessageLog> findAllMessages(Pageable pageable);

    /**
     * 캠페인별 메시지 로그 조회
     */
    @Query("SELECT m FROM MessageLog m WHERE m.campaignId = :campaignId ORDER BY m.createdAt DESC")
    Page<MessageLog> findByCampaignId(@Param("campaignId") Long campaignId, Pageable pageable);

    /**
     * 메시지 타입별 조회
     */
    @Query("SELECT m FROM MessageLog m WHERE m.messageType = :messageType ORDER BY m.createdAt DESC")
    Page<MessageLog> findByMessageType(@Param("messageType") MessageType messageType, Pageable pageable);

    /**
     * 생성자별 메시지 로그 조회
     */
    @Query("SELECT m FROM MessageLog m WHERE m.createdBy = :createdBy ORDER BY m.createdAt DESC")
    Page<MessageLog> findByCreatedBy(@Param("createdBy") Long createdBy, Pageable pageable);

    /**
     * 메시지 상세 조회 (ID로)
     */
    @Query("SELECT m FROM MessageLog m WHERE m.messageId = :messageId")
    Optional<MessageLog> findByMessageId(@Param("messageId") Long messageId);

    /**
     * 세그먼트별 메시지 로그 조회
     */
    @Query("SELECT m FROM MessageLog m WHERE m.segmentId = :segmentId ORDER BY m.createdAt DESC")
    Page<MessageLog> findBySegmentId(@Param("segmentId") Long segmentId, Pageable pageable);
}
