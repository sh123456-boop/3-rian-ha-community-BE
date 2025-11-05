package com.ktb.community.oauth.dto;

import com.ktb.community.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOauth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public CustomOauth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes == null ? Collections.emptyMap() : attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return String.valueOf(user.getRole());
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    public Long getUserId() {
        return user.getId();
    }

    public User getUser() {
        return user;
    }
}
