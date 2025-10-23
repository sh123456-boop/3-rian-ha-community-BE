package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Entity
@Table(name = "user_like_posts")
@IdClass(UserLikePostsId.class) // ID 클래스 지정
@EntityListeners(AuditingEntityListener.class) // CreatedDate 활성화
@NoArgsConstructor
public class UserLikePosts {

    @Id // 복합 키 1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id // 복합 키 2
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @CreatedDate
    @Column(name = "liked_at", nullable = false)
    private Instant likedAt;

    public UserLikePosts(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}
