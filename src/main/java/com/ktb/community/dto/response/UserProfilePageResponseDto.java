package com.ktb.community.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class UserProfilePageResponseDto {

    private final List<UserProfileDto> users;
    private final boolean hasNext;

    public UserProfilePageResponseDto(List<UserProfileDto> users, boolean hasNext) {
        this.users = users;
        this.hasNext = hasNext;
    }
}
