package com.ktb.community.oauth.handler;

import com.ktb.community.entity.RefreshEntity;
import com.ktb.community.oauth.dto.CustomOauth2User;
import com.ktb.community.repository.RefreshRepository;
import com.ktb.community.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;



    @Value("${spring.route.front}")
    String front;

    public CustomOAuth2SuccessHandler(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof CustomOauth2User customOauth2User)) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return;
        }

        Long userId = customOauth2User.getUserId();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("USER");

        String access = jwtUtil.createJwt("access", userId, role, 3 * 600000L);
        String refresh = jwtUtil.createJwt("refresh", userId, role, 3 * 86400000L);

        addRefreshEntity(userId, refresh, 3 * 86400000L);

        ResponseCookie cookie = ResponseCookie.from("refresh", refresh)
                .path("/")
                .maxAge(24 * 60 * 60 * 3)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        String redirectUrl = UriComponentsBuilder
                .fromUriString(front +"/posts")
                .queryParam("access", URLEncoder.encode(access, StandardCharsets.UTF_8))
                .build(true)      // 이미 인코딩돼 있다면 true 옵션으로 재인코딩 방지
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private void addRefreshEntity(Long id, String refresh, Long expiredMs) {
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .userId(id)
                .refresh(refresh)
                .expiration(date.toString())
                .build();

        refreshRepository.save(refreshEntity);
    }
}
