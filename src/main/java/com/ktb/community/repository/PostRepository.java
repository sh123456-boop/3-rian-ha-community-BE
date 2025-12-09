package com.ktb.community.repository;

import com.ktb.community.entity.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {


    Slice<Post> findByOrderByIdDesc(Pageable pageable);

    // 다음 페이지 로딩: 특정 ID(커서)보다 작은 ID들을 내림차순으로 정렬하여 상위 N개 조회

    Slice<Post> findByIdLessThanOrderByIdDesc(Long lastPostId, Pageable pageable);


    // 👇 최신순 정렬
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH u.image " +
            "LEFT JOIN FETCH p.postCount c " +
            "ORDER BY p.id DESC")
    Slice<Post> findSliceByOrderByIdDesc(Pageable pageable);

    // 👇 최신순 정렬
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH u.image " +
            "LEFT JOIN FETCH p.postCount c " +
            "WHERE p.id < :lastPostId ORDER BY p.id DESC")
    Slice<Post> findSliceByIdLessThanOrderByIdDesc(@Param("lastPostId") Long lastPostId, Pageable pageable);


    // 👇 인기순 정렬
    @Query("SELECT DISTINCT p FROM Post p " +
                  "LEFT JOIN FETCH p.user u " +
                  "LEFT JOIN FETCH u.image " +
                  "LEFT JOIN FETCH p.postCount c " + // 👈 postcount -> postCount (필드명 수정)
                  "ORDER BY c.view_cnt DESC, p.id DESC") // 👈 2차 정렬 기준 추가
    Slice<Post> findSliceByOrderByViewCountDesc(Pageable pageable);

    // 👇 인기순 정렬
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH u.image " +
            "LEFT JOIN FETCH p.postCount c " + // 👈 postcount -> postCount (필드명 수정)
            "WHERE (c.view_cnt < :lastViewCount) OR (c.view_cnt = :lastViewCount AND p.id < :lastPostId) " + // 👈 페이지네이션 조건 수정
            "ORDER BY c.view_cnt DESC, p.id DESC") // 👈 postcount -> view_cnt, 2차 정렬 기준 추가
    Slice<Post> findSliceByOrderByViewCountDesc(@Param("lastViewCount") Long lastViewCount, @Param("lastPostId") Long lastPostId, Pageable pageable);


    // ✅ N+1 테스트를 위한 새로운 메서드 (Fetch Join 없음)
    @Query("SELECT p FROM Post p ORDER BY p.id DESC")
    Slice<Post> findSliceWithoutFetchJoinOrderByIdDesc(Pageable pageable);

    // 비관적 락을 사용하여 Post와 PostCount를 함께 조회
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p JOIN FETCH p.postCount WHERE p.id = :postId")
    Optional<Post> findByIdWithPessimisticLock(@Param("postId") Long postId);

    // 낙관적 락을 사용하여 Post와 PostCount를 함께 조회
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Post p JOIN FETCH p.postCount WHERE p.id = :postId")
    Optional<Post> findByIdWithOptimisticLock(@Param("postId") Long postId);

}
