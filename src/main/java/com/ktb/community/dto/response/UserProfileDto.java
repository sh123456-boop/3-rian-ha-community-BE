package com.ktb.community.dto.response;

import lombok.Getter;

@Getter
public class UserProfileDto {

    private final Long userId;
    private final String nickname;
    private final String profileImageUrl;

    public UserProfileDto(Long userId, String nickname, String profileImageUrl) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
