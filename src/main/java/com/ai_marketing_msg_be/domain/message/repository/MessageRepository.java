package com.ai_marketing_msg_be.domain.message.repository;

import com.ai_marketing_msg_be.domain.message.entity.Message;
import com.ai_marketing_msg_be.domain.message.entity.MessageType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            SELECT m FROM Message m
            LEFT JOIN FETCH m.user
            LEFT JOIN FETCH m.campaign
            LEFT JOIN FETCH m.product
            LEFT JOIN FETCH m.segment
            LEFT JOIN FETCH m.customer
            WHERE m.messageId = :messageId
            """)
    Optional<Message> findByMessageIdWithDetails(@Param("messageId") Long messageId);


    @Query("""
            SELECT m FROM Message m
            LEFT JOIN FETCH m.campaign
            LEFT JOIN FETCH m.product
            ORDER BY m.createdAt DESC
            """)
    Page<Message> findAllWithDetails(Pageable pageable);


    Page<Message> findByMessageType(MessageType messageType, Pageable pageable);


    @Query("""
            SELECT m FROM Message m
            WHERE m.user.id = :userId
            ORDER BY m.createdAt DESC
            """)
    Page<Message> findByUserId(@Param("userId") Long userId, Pageable pageable);


    Page<Message> findByCampaign_CampaignId(Long campaignId, Pageable pageable);

    @Query("SELECT COUNT(m) > 0 FROM Message m WHERE m.product.productId = :productId")
    boolean existsByProductId(@Param("productId") Long productId);
}