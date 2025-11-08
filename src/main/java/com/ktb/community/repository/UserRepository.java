package com.ktb.community.repository;

import com.ktb.community.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    User findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
