package com.ktb.community.chat.repository;

import com.ktb.community.chat.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
}
