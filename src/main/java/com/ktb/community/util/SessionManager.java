package com.ktb.community.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class SessionManager {

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    private final long sessionTtlSeconds;

    public SessionManager(@Value("${app.session.ttl-seconds}") long sessionTtlSeconds) {
        this.sessionTtlSeconds = sessionTtlSeconds;
    }

    // 세션 키, value(사용자 아이디, 만료시간) 생성 및 저장 - 로그인에서 사용
    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(sessionTtlSeconds, ChronoUnit.SECONDS);
        sessions.put(sessionId, new SessionData(userId, expiresAt));
        return sessionId;
    }

    // 세션 아이디로 사용자 아이디 조회 - 필터단에서 사용
    public Optional<Long> findUserId(String sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }

        SessionData session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }

        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(sessionId);
            return Optional.empty();
        }

        return Optional.of(session.userId());
    }

    // 세션 삭제 - 로그아웃, 회원탈퇴에서 사용
    public void removeSession(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }

    public long getSessionTtlSeconds() {
        return sessionTtlSeconds;
    }

    private record SessionData(Long userId, Instant expiresAt) {
    }
}
