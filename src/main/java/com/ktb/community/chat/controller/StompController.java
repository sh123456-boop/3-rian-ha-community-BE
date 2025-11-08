package com.ktb.community.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ktb.community.chat.dto.ChatMessageDto;
import com.ktb.community.chat.service.ChatServiceImpl;
import com.ktb.community.chat.service.RedisPubSubService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatServiceImpl chatServiceImpl;
    private final RedisPubSubService pubSubService;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatServiceImpl chatServiceImpl, RedisPubSubService pubSubService) {
        this.messageTemplate = messageTemplate;
        this.chatServiceImpl = chatServiceImpl;
        this.pubSubService = pubSubService;
    }

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatmessageDto) throws JsonProcessingException {
//        /publish/roomId 형태로 오면 해당 room에 메세지 저장
        chatServiceImpl.saveMessage(roomId, chatmessageDto);
        chatmessageDto.setRoomId(roomId);
    }
}
