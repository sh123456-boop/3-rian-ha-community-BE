package com.ktb.community.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.ktb.community.util.SessionConstants.SESSION_COOKIE_NAME;
import static com.ktb.community.util.SessionConstants.USER_ID_ATTRIBUTE;

public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionManager sessionManager;
    private final List<String> excludedPatterns;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public SessionAuthenticationFilter(SessionManager sessionManager,
                                       List<String> excludedPatterns) {
        this.sessionManager = sessionManager;
        this.excludedPatterns = excludedPatterns;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 브라우저의 preflight 요청에 대한 처리                                
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // "/v1/users/me/nickname"의 post, put 요청은 제외하지 않도록 처리
        if (isExcluded(request)) {
            // get 요청이면 세션 아이디 검증 없이 통과
            filterChain.doFilter(request, response);
            return;
        }

        // 세션 쿠키에서 세션 ID 추출
        String sessionId = extractSessionId(request.getCookies());

        // 세션 ID로 사용자 ID 조회
        Long userId = sessionManager.findUserId(sessionId).orElse(null);

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        request.setAttribute(USER_ID_ATTRIBUTE, userId);
        filterChain.doFilter(request, response);
    }

    // "/v1/users/me/nickname"의 post, put 요청은 제외하지 않도록 처리
    private boolean isExcluded(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if ("/v1/users/me/nickname".equals(requestUri) && !HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        return excludedPatterns.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, requestUri));
    }

    // 세션 쿠키에서 세션 ID 추출
    private String extractSessionId(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equalsIgnoreCase(SESSION_COOKIE_NAME))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
