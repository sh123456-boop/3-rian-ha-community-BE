package com.ktb.community.util;

public final class SessionConstants {

    private SessionConstants() {
    }

    // 세션 쿠키 이름
    public static final String SESSION_COOKIE_NAME = "sessionId";

    // request 속성에 저장되는 사용자 ID 키 이름
    public static final String USER_ID_ATTRIBUTE = "authenticatedUserId";
}
