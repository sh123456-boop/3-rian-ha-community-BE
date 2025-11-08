package com.ktb.community.chat.repository;

import com.ktb.community.chat.entity.ChatRoom;
import com.ktb.community.chat.entity.ReadStatus;
import com.ktb.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
    List<ReadStatus> findByChatRoomAndUser(ChatRoom chatRoom, User user);
    Long countByChatRoomAndUserAndIsReadFalse(ChatRoom chatRoom, User user);
}
