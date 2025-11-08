package com.ktb.community.chat.repository;

import com.ktb.community.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Page<ChatRoom> findByIsGroupChatTrue(Pageable pageable);

    Optional<ChatRoom> findByName(String name);
}
