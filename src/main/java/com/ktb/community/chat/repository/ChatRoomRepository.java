package com.ktb.community.chat.repository;

import com.ktb.community.chat.entity.ChatRoom;
import com.ktb.community.chat.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByIsGroupChat(boolean isGroupChat);
}
