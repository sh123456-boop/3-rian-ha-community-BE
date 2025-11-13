package com.ktb.community.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessagePubSubDto {

    private Long roomId;
    private String message;
    private String nickName;
    private Long senderId;

}
