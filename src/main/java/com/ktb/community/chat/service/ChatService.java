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

    // 유저가 해당 채팅방 참여자인지 확인
    boolean isRoomPaticipant(Long userId, Long roomId);

    // 메시지 읽음 처리
    void messageRead(Long roomId, Long userId);

    // 내 채팅 목록 조회
    List<MyChatListResDto> getMyChatRooms(Long userId);

    // 그룹 채팅방 나가기
    void leaveGroupChatRoom(Long roomId, Long userId);

    // 1:1 채팅방 개설
    Long getOrCreatePrivateRoom(Long otherMemberId, Long userId);
}
