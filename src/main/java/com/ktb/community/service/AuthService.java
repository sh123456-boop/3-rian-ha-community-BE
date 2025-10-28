package com.ktb.community.service;

import com.ktb.community.dto.request.JoinRequestDto;
import com.ktb.community.dto.request.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    // 회원가입
    void join(JoinRequestDto dto);

    void login(LoginRequestDto dto, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

}
