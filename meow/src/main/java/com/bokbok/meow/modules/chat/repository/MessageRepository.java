package com.bokbok.meow.modules.chat.repository;

import com.bokbok.meow.modules.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<Message> findByConversationId(@Param("conversationId") String conversationId);

    // For pagination — load older messages
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByConversationIdPaged(
            @Param("conversationId") String conversationId,
            Pageable pageable
    );

    // Undelivered messages for a user (to send on reconnect)
    @Query("SELECT m FROM Message m WHERE " +
            "m.conversation.id IN (" +
            "  SELECT c.id FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId" +
            ") AND m.sender.id != :userId AND m.status = 'SENT'")
    List<Message> findUndeliveredMessages(@Param("userId") String userId);
}