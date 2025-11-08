package com.ktb.community.chat.repository;

import com.ktb.community.chat.entity.ChatParticipant;
import com.ktb.community.chat.entity.ChatRoom;
import com.ktb.community.chat.entity.ReadStatus;
import com.ktb.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User user);
}
