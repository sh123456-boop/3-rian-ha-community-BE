package com.ktb.community.oauth.dto;

import java.util.Collections;
import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;
    private final Map<String, Object> properties;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = extractMap(attributes, "kakao_account");
        this.profile = extractMap(kakaoAccount, "profile");
        this.properties = extractMap(attributes, "properties");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        Object providerId = attributes.get("id");
        return providerId != null ? providerId.toString() : "";
    }

    @Override
    public String getEmail() {
//        Object email = kakaoAccount.get("email");
//        if (email == null) {
//            email = attributes.get("email");
//        }
//        return email != null ? email.toString() : "";
        return "@kakao.com";
    }

    @Override
    public String getNickname() {
        Object nickname = profile.get("nickname");
        if (nickname == null) {
            nickname = properties.get("nickname");
        }
        return nickname != null ? nickname.toString() : "";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractMap(Map<String, Object> source, String key) {
        if (source == null) {
            return Collections.emptyMap();
        }
        Object value = source.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }
}
