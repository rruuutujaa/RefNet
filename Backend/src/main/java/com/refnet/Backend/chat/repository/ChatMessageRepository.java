package com.refnet.Backend.chat.repository;

import com.refnet.Backend.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.sender.id = :user1 AND m.receiver.id = :user2) OR " +
           "(m.sender.id = :user2 AND m.receiver.id = :user1) " +
           "ORDER BY m.createdAt DESC, m.id DESC")
    Page<ChatMessage> findConversation(@Param("user1") UUID user1, @Param("user2") UUID user2, Pageable pageable);

    @Query(value = "SELECT DISTINCT ON (partner_id) * FROM (" +
           "  SELECT receiver_id as partner_id, created_at, content, id, sender_id, is_read FROM chat_messages WHERE sender_id = :userId " +
           "  UNION ALL " +
           "  SELECT sender_id as partner_id, created_at, content, id, sender_id, is_read FROM chat_messages WHERE receiver_id = :userId " +
           ") as sub " +
           "ORDER BY partner_id, created_at DESC", nativeQuery = true)
    List<ChatMessage> findRecentConversations(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    void markAsRead(@Param("senderId") UUID senderId, @Param("receiverId") UUID receiverId);
}
