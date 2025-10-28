package com.ktb.community.config;

import com.ktb.community.config.filter.JwtAuthenticationFilter;
import com.ktb.community.repository.RefreshRepository;
import com.ktb.community.util.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

@Configuration
public class JwtFilterConfig {

    private static final List<String> EXCLUDED_PATTERNS = List.of(
            "/v1/auth/login",
            "/v1/auth/join",
            "/v1/auth/reissue",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/terms",
            "/privacy",
            "/v1/users/me/nickname"
    );

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter(JWTUtil jwtUtil,
                                                                                   RefreshRepository refreshRepository,
                                                                                   @Value("${jwt.access-expiration-ms}") long accessTokenExpiryMs,
                                                                                   @Value("${jwt.refresh-expiration-ms}") long refreshTokenExpiryMs) {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtUtil,
                refreshRepository,
                EXCLUDED_PATTERNS,
                accessTokenExpiryMs,
                refreshTokenExpiryMs
        );

        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
