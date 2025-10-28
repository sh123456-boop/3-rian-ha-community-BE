package com.ktb.community.service;

import com.ktb.community.dto.request.JoinRequestDto;
import com.ktb.community.dto.request.LoginRequestDto;
import com.ktb.community.entity.RefreshEntity;
import com.ktb.community.entity.Role;
import com.ktb.community.entity.User;
import com.ktb.community.exception.BusinessException;
import com.ktb.community.exception.ErrorCode;
import com.ktb.community.repository.RefreshRepository;
import com.ktb.community.repository.UserRepository;
import com.ktb.community.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

import static com.ktb.community.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String ACCESS_COOKIE_NAME = "access";
    private static final String REFRESH_COOKIE_NAME = "refresh";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshRepository refreshRepository;
    private final JWTUtil jwtUtil;

    @Value("${jwt.access-expiration-ms:1800000}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-expiration-ms:259200000}")
    private long refreshTokenExpiryMs;

    @Override
    public void join(JoinRequestDto dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        String rePassword = dto.getRePassword();
        String nickname = dto.getNickname();

        if (!password.equals(rePassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(EMAIL_DUPLICATION);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(NICKNAME_DUPLICATION);
        }

        User user = User.builder()
                .nickname(nickname)
                .password(bCryptPasswordEncoder.encode(password))
                .email(email)
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }

    @Override
    public void login(LoginRequestDto dto, HttpServletResponse response) {
        User user = Optional.ofNullable(userRepository.findByEmail(dto.getEmail()))
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!bCryptPasswordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        issueTokens(response, user.getId(), user.getRole().name());
    }


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveCookie(request, REFRESH_COOKIE_NAME);

        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(ACCESS_DENIED);
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(UNAUTHORIZED_USER);
        }

        if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
            throw new BusinessException(UNAUTHORIZED_USER);
        }

        if (!refreshRepository.existsByRefresh(refreshToken)) {
            throw new BusinessException(UNAUTHORIZED_USER);
        }

        refreshRepository.deleteByRefresh(refreshToken);

        expireCookie(response, ACCESS_COOKIE_NAME);
        expireCookie(response, REFRESH_COOKIE_NAME);
    }

    private void issueTokens(HttpServletResponse response, Long userId, String role) {

        // 로그인 시 기존 userId와 연결된 refresh 토큰 삭제
        refreshRepository.deleteByUserId(userId);

        String accessToken = jwtUtil.createJwt("access", userId, role, accessTokenExpiryMs);
        String refreshToken = jwtUtil.createJwt("refresh", userId, role, refreshTokenExpiryMs);

        saveRefreshToken(userId, refreshToken);

        writeCookie(response, ACCESS_COOKIE_NAME, accessToken, accessTokenExpiryMs);
        writeCookie(response, REFRESH_COOKIE_NAME, refreshToken, refreshTokenExpiryMs);
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        String expiration = new Date(System.currentTimeMillis() + refreshTokenExpiryMs).toString();

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .userId(userId)
                .refresh(refreshToken)
                .expiration(expiration)
                .build();

        refreshRepository.save(refreshEntity);
    }

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

    private void expireCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
