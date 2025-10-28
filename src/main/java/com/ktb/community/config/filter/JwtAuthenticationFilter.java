package com.ktb.community.config.filter;

import com.ktb.community.repository.RefreshRepository;
import com.ktb.community.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;


public class JwtAuthenticationFilter implements Filter {

    public static final String USER_ID_ATTRIBUTE = "userId";
    private static final String ACCESS_COOKIE_NAME = "access";
    private static final String REFRESH_COOKIE_NAME = "refresh";

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final List<String> excludedPatterns;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JWTUtil jwtUtil,
                                   RefreshRepository refreshRepository,
                                   List<String> excludedPatterns,
                                   long accessTokenExpiryMs,
                                   long refreshTokenExpiryMs) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.excludedPatterns = excludedPatterns;
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 화이트 리스트는 검증하지 않음
        if (shouldSkip(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // 1차로 access 토큰 검증
        try {
            Long userId = authenticateWithAccessToken(httpRequest);
            if (userId != null) {
                httpRequest.setAttribute(USER_ID_ATTRIBUTE, userId);
                chain.doFilter(request, response);
                return;
            }
        } catch ( JwtException | IllegalArgumentException ignored) {
            // 토큰이 유효하지 않은 경우 다음 단계로 진행
        }

        Long userId = authenticateWithAccessToken(httpRequest);
        if (userId == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            return;
        }

        // 1차 검증 실패시 2차로 refresh 토큰 검증
        userId = authenticateWithRefreshToken(httpRequest, httpResponse);
        if (userId == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            return;
        }

        httpRequest.setAttribute(USER_ID_ATTRIBUTE, userId);
        chain.doFilter(request, response);
    }

    // 화이트 리스트는 건너뜀
    private boolean shouldSkip(HttpServletRequest request) {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String requestUri = request.getRequestURI();
        return excludedPatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    // access 토큰으로 인증 시도
    private Long authenticateWithAccessToken(HttpServletRequest request) {
        // 쿠키에서 access 토큰 추출
        String accessToken = resolveCookie(request, ACCESS_COOKIE_NAME);
        // 토큰 존재 여부 확인
        // null, 빈 문자열, 공백만 있는 문자열 확인 가능
        if (!StringUtils.hasText(accessToken)) {
            return null;
        }

        // 만료기한 확인
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException ex) {
            return null;
        }
        // 카테고리 확인
        if (!"access".equals(jwtUtil.getCategory(accessToken))) {
            return null;
        }

        return jwtUtil.getID(accessToken);
    }

    // refresh 토큰으로 인증 시도
    private Long authenticateWithRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveCookie(request, REFRESH_COOKIE_NAME);
        // 토큰 존재 여부 확인
        if (!StringUtils.hasText(refreshToken)) {
            return null;
        }

        // 만료기한 확인
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException ex) {
            return null;
        }

        // 카테고리 확인
        if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
            return null;
        }

        // DB 존재 여부 확인
        if (!refreshRepository.existsByRefresh(refreshToken)) {
            return null;
        }

        Long userId = jwtUtil.getID(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 새로운 토큰 발급
        String newAccessToken = jwtUtil.createJwt("access", userId, role, accessTokenExpiryMs);
        String newRefreshToken = jwtUtil.createJwt("refresh", userId, role, refreshTokenExpiryMs);

        String expiration = new Date(System.currentTimeMillis() + refreshTokenExpiryMs).toString();
        // 새로운 토큰으로 레포지토리 update
        int updated = refreshRepository.rotateRefresh(userId, refreshToken, newRefreshToken, expiration);
        // 실패 시
        if (updated == 0) {
            // 사용하지 않을 토큰(비정상) 삭제
            refreshRepository.deleteByRefresh(refreshToken);
            return null;
        }

        // 재발급 된 토큰 쿠키에 셋팅
        writeCookie(response, ACCESS_COOKIE_NAME, newAccessToken, accessTokenExpiryMs);
        writeCookie(response, REFRESH_COOKIE_NAME, newRefreshToken, refreshTokenExpiryMs);

        return userId;
    }

    // 쿠키 조회
    private String resolveCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // 쿠키 작성
    private void writeCookie(HttpServletResponse response, String name, String value, long expiryMs) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(expiryMs))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
