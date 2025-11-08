package com.ktb.community.chat.service;

import com.ktb.community.chat.dto.ChatMessageDto;
import com.ktb.community.chat.dto.ChatRoomListResDto;
import com.ktb.community.chat.dto.MyChatListResDto;
import com.ktb.community.chat.entity.ChatMessage;
import com.ktb.community.chat.entity.ChatParticipant;
import com.ktb.community.chat.entity.ChatRoom;
import com.ktb.community.chat.entity.ReadStatus;
import com.ktb.community.chat.repository.ChatMessageRepository;
import com.ktb.community.chat.repository.ChatParticipantRepository;
import com.ktb.community.chat.repository.ChatRoomRepository;
import com.ktb.community.chat.repository.ReadStatusRepository;
import com.ktb.community.entity.User;
import com.ktb.community.exception.BusinessException;
import com.ktb.community.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ktb.community.exception.ErrorCode.*;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    public ChatServiceImpl(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ReadStatusRepository readStatusRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.readStatusRepository = readStatusRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void saveMessage(Long roomId, ChatMessageDto chatMessageReqDto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new BusinessException(ROOM_NOT_FOUND));

        // 보낸사람 조회
        User sender = userRepository.findById(chatMessageReqDto.getSenderId()).orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .contents(chatMessageReqDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        // 사용자 별로 읽음 여부 저장(보낸사람만 읽음 표시)
        List<ChatParticipant> chatParticipantList = chatRoom.getChatParticipantList();
        for (ChatParticipant p : chatParticipantList) {
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .user(p.getUser())
                    .chatMessage(chatMessage)
                    .isRead(p.getUser().equals(sender))
                    .build();
            readStatusRepository.save(readStatus);
        }

    }

    @Override
    public void createGroupRoom(String chatRoomName, Long userId) {
        // 현재 사용자 확인
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 그룹 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomName)
                .isGroupChat(true)
                .build();
        chatRoomRepository.save(chatRoom);

        // 채팅 참여자로 개설자 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    @Override
    public List<ChatRoomListResDto> getGroupchatRooms() {
        // 레포지토리에서 그룹채팅방 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat(true);

        // DTO로 변환
        return chatRooms.stream()
                .map(chatRoom -> ChatRoomListResDto.builder()
                        .roomId(chatRoom.getId())
                        .roomName(chatRoom.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void addParticipantToGroupChat(Long roomId, Long userId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new BusinessException(ROOM_NOT_FOUND));

        // 유저 조회
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 이미 참여자인지 검증
        Optional<ChatParticipant> byChatRoomAndUser = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user);
        if (!byChatRoomAndUser.isPresent()) {
            addParticipantToRoom(chatRoom, user);
        }
    }

    @Override
    public void addParticipantToRoom(ChatRoom chatRoom, User user) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    @Override
    public List<ChatMessageDto> getChatHistory(Long roomId, Long userId) {
        // 해당 채팅방의 참여자가 아닐 경우 에러 반환
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new BusinessException(ROOM_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));
        Optional<ChatParticipant> byChatRoomAndUser = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user);
        if (!byChatRoomAndUser.isPresent()) {
            throw new BusinessException(ACCESS_DENIED);
        }
        // 특정 room에 대한 메시지 조회
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for(ChatMessage c : chatMessages){
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .message(c.getContents())
                    .senderId(c.getUser().getId())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }

    @Override
    public boolean isRoomPaticipant(Long userId, Long roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new BusinessException(ROOM_NOT_FOUND));

        // 유저 조회
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 이미 참여자인지 검증
        Optional<ChatParticipant> byChatRoomAndUser = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user);
        if (byChatRoomAndUser.isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public void messageRead(Long roomId) {

    }

    @Override
    public List<MyChatListResDto> getMyChatRooms() {
        return List.of();
    }

    @Override
    public void leaveGroupChatRoom(Long roomId) {

    }

    @Override
    public Long getOrCreatePrivateRoom(Long otherMemberId) {
        return 0L;
    }
}
