package com.ktb.community.chat.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomPageResponseDto {

    private final List<ChatRoomResDto> rooms;
    private final boolean hasNext;

    public ChatRoomPageResponseDto(List<ChatRoomResDto> rooms, boolean hasNext) {
        this.rooms = rooms;
        this.hasNext = hasNext;
    }

}
