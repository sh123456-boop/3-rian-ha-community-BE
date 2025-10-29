package com.ktb.community.repository;

import com.ktb.community.entity.Comment;
import com.ktb.community.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user u " +
            "LEFT JOIN FETCH u.image " +
            "WHERE c.post.id = :postId " +
            "ORDER BY c.id DESC")
    Slice<Comment> findSliceByPostIdOrderByIdDesc(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user u " +
            "LEFT JOIN FETCH u.image " +
            "WHERE c.post.id = :postId AND c.id < :lastCommentId " +
            "ORDER BY c.id DESC")
    Slice<Comment> findSliceByPostIdAndIdLessThanOrderByIdDesc(@Param("postId") Long postId,
                                                               @Param("lastCommentId") Long lastCommentId,
                                                               Pageable pageable);

    void deleteByUser(User user);

    @Query("SELECT new com.ktb.community.repository.PostCommentCountDto(c.post.id, COUNT(c.id)) " +
            "FROM Comment c " +
            "WHERE c.user.id = :userId " +
            "GROUP BY c.post.id")
    List<PostCommentCountDto> countCommentsByUserGroupedByPost(@Param("userId") Long userId);
}
