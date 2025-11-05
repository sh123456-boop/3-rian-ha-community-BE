package com.ktb.community.oauth.service;

import ch.qos.logback.core.joran.conditional.IfAction;
import com.ktb.community.entity.OauthUser;
import com.ktb.community.entity.Role;
import com.ktb.community.entity.User;
import com.ktb.community.oauth.dto.CustomOauth2User;
import com.ktb.community.oauth.dto.NaverResponse;
import com.ktb.community.oauth.dto.OAuth2Response;
import com.ktb.community.repository.OauthUserRepository;
import com.ktb.community.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OauthUserRepository oauthUserRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, OauthUserRepository oauthUserRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.oauthUserRepository = oauthUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        // 네이버에서 온 응답일때
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }

        String provider = oAuth2Response.getProvider();
        String providerId = oAuth2Response.getProviderId();
        String email = provider+oAuth2Response.getEmail();

        OauthUser existData = oauthUserRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElse(null);
        User user;
        // 기존 데이터가 없으면 user, oauthuser 생성 후 저장
        if (existData == null) {
            // Oauthuser 엔티티 생성
            OauthUser oauthUser = new OauthUser();

            // user 엔티티 생성
            User newUser = User.builder()
                    .oauthUser(oauthUser)
                    .email(email)
                    .password(passwordEncoder.encode("크"))
                    .role(Role.USER)
                    .nickname(oAuth2Response.getNickname())
                    .build();

            // Oauthuser 엔티티 생성
            oauthUser.setUser(newUser);
            oauthUser.setProvider(provider);
            oauthUser.setProviderId(providerId);

            // user 저장 후 oauthuser 저장 (연관관계 주인인 oauthUser를 나중에 저장)
            User savedUser = userRepository.save(newUser);
            oauthUser.setUser(savedUser);
            oauthUserRepository.save(oauthUser);
            user = savedUser;
        } else {
            // 기존 데이터가 있으면 nickname 수정만
            user = existData.getUser();
        }

        return new CustomOauth2User(user, oAuth2User.getAttributes());
    }
}
