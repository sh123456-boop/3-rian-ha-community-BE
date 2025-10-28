package com.ktb.community.service;

import com.ktb.community.dto.request.JoinRequestDto;
import com.ktb.community.dto.request.LoginRequestDto;
import com.ktb.community.entity.Role;
import com.ktb.community.entity.User;
import com.ktb.community.exception.BusinessException;
import com.ktb.community.repository.UserRepository;
import com.ktb.community.util.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ktb.community.exception.ErrorCode.EMAIL_DUPLICATION;
import static com.ktb.community.exception.ErrorCode.NICKNAME_DUPLICATION;
import static com.ktb.community.exception.ErrorCode.PASSWORD_MISMATCH;
import static com.ktb.community.exception.ErrorCode.UNAUTHORIZED_USER;
import static com.ktb.community.util.SessionConstants.SESSION_COOKIE_NAME;
import static com.ktb.community.util.SessionRequestUtils.findSessionId;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SessionManager sessionManager;

    @Value("${app.session.cookie-domain}")
    private String cookieDomain;

    @Value("${app.session.cookie-secure}")
    private boolean cookieSecure;

    @Value("${app.session.cookie-same-site}")
    private String cookieSameSite;

    @Override
    public void join(JoinRequestDto dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        String rePassword = dto.getRePassword();
        String nickname = dto.getNickname();


        if (!password.equals(rePassword)) {
            throw new BusinessException(PASSWORD_MISMATCH);
        }

        boolean isExistEmail = userRepository.existsByEmail(email);
        boolean isExistNickname = userRepository.existsByNickname(nickname);
        if (isExistEmail) {
            throw new BusinessException(EMAIL_DUPLICATION);
        }
        if (isExistNickname) {
            throw new BusinessException(NICKNAME_DUPLICATION);
        }

        User user = User.builder()
                .nickname(nickname)
                .password(bCryptPasswordEncoder.encode(password))
                .email(email)
                .role(Role.valueOf("USER"))
                .build();
        userRepository.save(user);
    }

    @Override
    public void login(LoginRequestDto dto, HttpServletResponse response) {
        User user = userRepository.findByEmail(dto.getEmail());
        if (user == null) {
            throw new BusinessException(UNAUTHORIZED_USER);
        }

        // 솔트 처리된 비밀번호 비교
        if (!bCryptPasswordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(UNAUTHORIZED_USER);
        }

        // 세션 키, value(사용자 아이디, 만료시간) 생성 및 저장
        String sessionId = sessionManager.createSession(user.getId());

        // 조건에 맞는 쿠키 생성 및 응답 헤더에 추가
        ResponseCookie cookie = buildSessionCookie(sessionId);
        response.addHeader("Set-Cookie", cookie.toString());
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = findSessionId(request);
        if (sessionId == null) {
            throw new BusinessException(UNAUTHORIZED_USER);
        }

        sessionManager.removeSession(sessionId);
        ResponseCookie cookie = buildExpiredCookie();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    // 쿠키 조건 설정 
    private ResponseCookie buildSessionCookie(String sessionId) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .maxAge(sessionManager.getSessionTtlSeconds())
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    // null 값의 만료된 쿠키 생성
    private ResponseCookie buildExpiredCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(SESSION_COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .maxAge(0)
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }
}
