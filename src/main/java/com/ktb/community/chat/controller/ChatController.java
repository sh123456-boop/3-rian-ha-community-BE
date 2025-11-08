package com.ktb.community.chat.controller;

import com.ktb.community.chat.dto.ChatMessageDto;
import com.ktb.community.chat.dto.ChatRoomListResDto;
import com.ktb.community.chat.dto.MyChatListResDto;
import com.ktb.community.chat.service.ChatService;
import com.ktb.community.chat.service.ChatServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatServiceImpl chatService;

    public ChatController(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    //    그룹채팅방 개설
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom(@RequestParam String roomName,
                                             @AuthenticationPrincipal(expression = "userId") Long userId){
        chatService.createGroupRoom(roomName,userId);
        return ResponseEntity.ok().build();
    }

    //    그룹채팅목록조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getGroupChatRooms(){
        List<ChatRoomListResDto> chatRooms = chatService.getGroupchatRooms();
        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }

    //    그룹채팅방참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long roomId,
                                               @AuthenticationPrincipal(expression = "userId") Long userId){
        chatService.addParticipantToGroupChat(roomId, userId);
        return ResponseEntity.ok().build();
    }

    //    이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId,
                                            @AuthenticationPrincipal(expression = "userId") Long userId){
        List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(roomId, userId);
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }

    //    채팅메시지 읽음처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId,
                                         @AuthenticationPrincipal(expression = "userId") Long userId){
        chatService.messageRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    //    내채팅방목록조회 : roomId, roomName, 그룹채팅여부, 메시지읽음개수
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms(@AuthenticationPrincipal(expression = "userId") Long userId){
        List<MyChatListResDto> myChatListResDtos = chatService.getMyChatRooms(userId);
        return new ResponseEntity<>(myChatListResDtos, HttpStatus.OK);
    }

    //    채팅방 나가기
    @DeleteMapping("/room/group/{roomId}/leave")
    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long roomId,
                                                @AuthenticationPrincipal(expression = "userId") Long userId){
        chatService.leaveGroupChatRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    //    개인 채팅방 개설 또는 기존roomId return
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam Long otherMemberId,
                                                    @AuthenticationPrincipal(expression = "userId") Long userId){
        Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId, userId);
        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }
}
