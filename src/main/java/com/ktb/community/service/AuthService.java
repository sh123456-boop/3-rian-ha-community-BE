package com.ktb.community.service;

import com.ktb.community.dto.request.JoinRequestDto;
import com.ktb.community.dto.request.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    // 회원가입
    void join(JoinRequestDto dto);

    // 로그인
    void login(LoginRequestDto dto, HttpServletResponse response);

    // refresh 토큰으로 access 토큰 재발급
    // 필터 단에서 처리해서 서비스 로직에서는 제거
    // void reissue(HttpServletRequest request, HttpServletResponse response);

    // 로그아웃
    void logout(HttpServletRequest request, HttpServletResponse response);

}
