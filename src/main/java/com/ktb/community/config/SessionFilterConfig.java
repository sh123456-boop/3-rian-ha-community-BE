package com.ktb.community.config;

import com.ktb.community.util.SessionAuthenticationFilter;
import com.ktb.community.util.SessionManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

@Configuration
public class SessionFilterConfig {

    // 세션 필터에서 제외할 URL 패턴 목록(화이트리스트)
    private static final List<String> EXCLUDED_PATTERNS = List.of(
            "/v1/auth/login",
            "/v1/auth/join",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/terms",
            "/privacy",
            "/v1/users/me/nickname"
    );

    @Bean
    public FilterRegistrationBean<SessionAuthenticationFilter> sessionAuthenticationFilter(SessionManager sessionManager) {
        SessionAuthenticationFilter filter = new SessionAuthenticationFilter(sessionManager, EXCLUDED_PATTERNS);

        FilterRegistrationBean<SessionAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        // 필터 실행 순서를 CORS 필터 다음으로 설정
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        // 모든 요청에 대해서 필터 검증
        registration.addUrlPatterns("/*");
        return registration;
    }
}
