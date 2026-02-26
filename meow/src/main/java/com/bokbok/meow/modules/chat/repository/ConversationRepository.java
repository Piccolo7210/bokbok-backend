package com.bokbok.meow.modules.chat.repository;

import com.bokbok.meow.modules.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.user1.id = :userId1 AND c.user2.id = :userId2) OR " +
            "(c.user1.id = :userId2 AND c.user2.id = :userId1)")
    Optional<Conversation> findByUsers(
            @Param("userId1") String userId1,
            @Param("userId2") String userId2
    );

    @Query("SELECT c FROM Conversation c WHERE " +
            "c.user1.id = :userId OR c.user2.id = :userId " +
            "ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findAllByUserId(@Param("userId") String userId);
}