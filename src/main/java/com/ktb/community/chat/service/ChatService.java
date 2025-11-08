package com.ktb.community.chat.service;

import com.ktb.community.chat.dto.ChatMessageDto;
import com.ktb.community.chat.dto.ChatRoomListResDto;
import com.ktb.community.chat.dto.MyChatListResDto;
import com.ktb.community.chat.entity.ChatRoom;
import com.ktb.community.entity.User;

import java.util.List;

public interface ChatService {
    // 메시지 발행 시 메시지 저장
    void saveMessage(Long roomId, ChatMessageDto chatMessageReqDto);

    // 그룹 채팅방 생성
    void createGroupRoom(String chatRoomName, Long userId);

    // 그룹 채팅방 조회
    List<ChatRoomListResDto> getGroupchatRooms();

    // 그룹채팅방 참여
    void addParticipantToGroupChat(Long roomId, Long userId);

    //
    void addParticipantToRoom(ChatRoom chatRoom, User user);

    // 채팅방 이전 메시지 조회
    List<ChatMessageDto> getChatHistory(Long roomId, Long userId);

    //
    boolean isRoomPaticipant(Long userId, Long roomId);

    //
    void messageRead(Long roomId);

    // 내 채팅 목록 조회
    List<MyChatListResDto> getMyChatRooms();

    // 그룹 채팅방 나가기
    void leaveGroupChatRoom(Long roomId);

    //
    Long getOrCreatePrivateRoom(Long otherMemberId);
}
