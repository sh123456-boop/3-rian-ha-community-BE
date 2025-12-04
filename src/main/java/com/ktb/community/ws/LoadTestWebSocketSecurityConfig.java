package com.ktb.community.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class LoadTestWebSocketSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain loadTestWebSocketFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ 여기만 이렇게 바꾸면 됨
                .securityMatcher("/connect") // 또는 "/v1/chat/connect"
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
