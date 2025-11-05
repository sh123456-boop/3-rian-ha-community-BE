package com.ktb.community.repository;

import com.ktb.community.entity.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OauthUserRepository extends JpaRepository<OauthUser, Long> {
    Optional<OauthUser> findByProviderAndProviderId(String provider, String providerId);
}
