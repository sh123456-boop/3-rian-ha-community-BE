package com.ktb.community.util;

import com.ktb.community.exception.BusinessException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import static com.ktb.community.exception.ErrorCode.UNAUTHORIZED_USER;
import static com.ktb.community.util.SessionConstants.SESSION_COOKIE_NAME;
import static com.ktb.community.util.SessionConstants.USER_ID_ATTRIBUTE;

public final class SessionRequestUtils {

    private SessionRequestUtils() {
    }

    // request에서 사용자 아이디 조회, 없으면 예외 발생 (컨트롤러에서 사용)
    public static Long getRequiredUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(USER_ID_ATTRIBUTE);
        if (attribute instanceof Long userId) {
            return userId;
        }

        throw new BusinessException(UNAUTHORIZED_USER);
    }

    // request에서 세션 아이디 조회 (서비스에서 사용)
    public static String findSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new BusinessException(UNAUTHORIZED_USER);
        }

        for (Cookie cookie : cookies) {
            if (SESSION_COOKIE_NAME.equalsIgnoreCase(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new BusinessException(UNAUTHORIZED_USER);
    }
}
