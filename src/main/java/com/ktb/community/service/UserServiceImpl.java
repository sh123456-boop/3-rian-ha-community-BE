package com.ktb.community.service;

import com.ktb.community.dto.request.PasswordRequestDto;
import com.ktb.community.dto.response.LikedPostsResponseDto;
import com.ktb.community.dto.response.UserInfoResponseDto;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.User;
import com.ktb.community.entity.Image;
import com.ktb.community.entity.UserLikePosts;
import com.ktb.community.exception.BusinessException;
import com.ktb.community.repository.*;
import com.ktb.community.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.stream.Collectors;

import static com.ktb.community.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final CommentRepository commentRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final S3ServiceImpl s3Service;
    private final UserLikePostsRepository userLikePostsRepository; // 리포지토리 주입
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${aws.cloud_front.domain}")
    private String cloudfrontDomain;

    @Value("${aws.cloud_front.default-profile-image-key}")
    private String defaultProfileImageKey;

    // 회원 닉네임 수정
    @Transactional
    public void updateNickname(String nickname, Long userId) {
        // 1. 닉네임 중복 확인
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(NICKNAME_DUPLICATION);
        }

        // 2. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 3. 닉네임 업데이트
        user.updateNickname(nickname);
    }

    // 회원 닉네임 중복 검사
    @Transactional
    public boolean findNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            return false;
        }
        return true;
    }

    // 회원 비밀번호 수정
    @Transactional
    public void updatePassword(PasswordRequestDto dto, Long userId) {

        // 2. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 2. 비밀번호가 일치 여부 확인
        if (!dto.getPassword().equals(dto.getRePassword())) {
            throw new BusinessException(PASSWORD_MISMATCH);
        }

        // 3. 새 비밀번호를 암호화하여 업데이트
        user.updatePassword(bCryptPasswordEncoder.encode(dto.getPassword()));
    }


    // 회원 탈퇴
    @Transactional
    public void deleteUser(HttpServletRequest request, HttpServletResponse response, Long userId, String password) {
        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 2. 비밀번호 확인
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(PASSWORD_MISMATCH);
        }

        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    refresh = cookie.getValue();
                }
            }
        }

        //refresh null check
        if (refresh == null) {
            throw new BusinessException(ACCESS_DENIED);

        }
        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ACCESS_DENIED);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {

            throw new BusinessException(ACCESS_DENIED);
        }

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            //response status code
            throw new BusinessException(ACCESS_DENIED);
        }

        // 저장소에서 토큰 삭제
        refreshRepository.deleteByRefresh(refresh);
        refreshRepository.deleteAllByUserId(userId);

        // 사용자가 작성한 댓글의 수를 각 게시물의 PostCount에서 감소
        List<PostCommentCountDto> postCommentCounts = commentRepository.countCommentsByUserGroupedByPost(userId);
        postCommentCounts.forEach(postCommentCount -> {
            Long postId = postCommentCount.postId();
            long commentCount = postCommentCount.commentCount();
            // 각 게시물의 PostCount 엔티티를 찾아서 댓글 수 감소
            Post post = postRepository.findById(postId).orElseThrow(()-> new BusinessException(POST_NOT_FOUND));
            post.getPostCount().decreaseCmtCount(commentCount);
        });

        // 사용자가 좋아요한 게시글들의 좋아요 수 감소
        List<UserLikePosts> likedPosts = userLikePostsRepository.findByUserOrderByLikedAtDesc(user);
        likedPosts.forEach(userLikePost -> {
            Post likedPost = userLikePost.getPost();
            if (likedPost != null && likedPost.getPostCount() != null) {
                likedPost.getPostCount().decreaseLikesCount();
            }
        });

        removeUserPostsFromRanking(user);

        // 사용자가 작성한 댓글 제거
        commentRepository.deleteByUser(user);

        // 쿠키 값 초기화
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // 3. 사용자 삭제
        userRepository.delete(user);
    }

    // 회원 프로필 이미지 설정
    /**
     * 사용자의 프로필 이미지를 업데이트합니다.
     * 기존 이미지가 있으면 S3와 DB에서 삭제하고, 새로운 이미지로 교체합니다.
     * @param userId 현재 사용자 ID
     * @param s3Key S3에 업로드된 새로운 이미지의 Key
     */
    @Transactional
    public void updateProfileImage(Long userId, String s3Key) {
        // 1. 사용자 정보를 조회합니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 2. 기존 프로필 이미지가 있는지 확인합니다.
        Image oldImage = user.getImage();
        if (oldImage != null) {
            // 3. 있었다면 기존 이미지를 S3 버킷에서 삭제합니다.
            s3Service.deleteFile(oldImage.getS3Key());
        }

        // 4. 새로운 Image 엔티티를 생성하고 사용자 정보(user)를 넣어줍니다.
        Image newProfileImage = new Image(s3Key, user);

        // 5. 사용자의 프로필 이미지를 새로운 이미지로 교체
        // 이 한 줄로 인해 orphanRemoval(기존 이미지 삭제)과 cascade(새 이미지 저장)가 모두 동작
        user.updateProfileImage(newProfileImage);
    }

    // 프로필 이미지 삭제
    @Transactional
    public void deleteProfileImage(Long userId) {
        // 1. 사용자 정보를 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 2. 사용자의 현재 프로필 이미지를 확인
        Image profileImage = user.getImage();

        // 3. 프로필 이미지가 존재할 경우에만 삭제 로직을 실행
        if (profileImage != null) {
            // 3-1. S3 버킷에서 실제 이미지 파일을 삭제
            s3Service.deleteFile(profileImage.getS3Key());

            // 3-2. User 엔티티와 Image 엔티티의 연관관계를 끊음
            user.updateProfileImage(null);
        }
    }

    // 사용자가 좋아요 누른 모든 게시물의 id목록을 최신순으로 반환
    // 단순 조회이므로 readOnly = true 옵션으로 성능 최적화
    @Transactional(readOnly = true)
    public LikedPostsResponseDto getLikedPosts(Long userId) {
        // 1. 사용자 정보를 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 2. 해당 사용자의 '좋아요' 기록을 liked_at 최신순으로 조회
        List<UserLikePosts> likedPosts = userLikePostsRepository.findByUserOrderByLikedAtDesc(user);

        // 3. 조회된 '좋아요' 기록 리스트에서 Post ID만 추출하여 새로운 리스트로 만듭니다.
        List<Long> postIds = likedPosts.stream()
                .map(userLikePosts -> userLikePosts.getPost().getId())
                .collect(Collectors.toList());

        return new LikedPostsResponseDto(postIds);
    }

    // 사용자 정보 페이지
    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        // 작성자의 프로필 이미지 URL을 생성
        String authorProfileImageUrl;

        if (user.getImage() != null) {
            // 유저의 프로필 이미지가 있으면 -> 해당 이미지의 URL 생성
            String s3Key = user.getImage().getS3Key();
            authorProfileImageUrl = "https://" + cloudfrontDomain + "/" + s3Key;
        } else {
            // 유저의 프로필 이미지가 없으면 -> 설정해둔 기본 이미지 URL 사용
            authorProfileImageUrl = "https://" + cloudfrontDomain + "/" + defaultProfileImageKey;
        }
        return new UserInfoResponseDto(user.getNickname(), user.getEmail(),authorProfileImageUrl, user.getId());
    }

    // 유저 삭제시 유저가 작성한 게시물도 레디스에서 제거
    private void removeUserPostsFromRanking(User user) {
        List<Post> userPosts = user.getPostList();
        if (userPosts == null || userPosts.isEmpty()) {
            return;
        }

        String dailyKey = getDailyRankingKey();
        String weeklyKey = getWeeklyRankingKey();
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();

        userPosts.stream()
                .map(Post::getId)
                .filter(postId -> postId != null)
                .map(String::valueOf)
                .forEach(postId -> {
                    zSetOperations.remove(dailyKey, postId);
                    zSetOperations.remove(weeklyKey, postId);
                });
    }

    private String getDailyRankingKey() {
        return "ranking:daily:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String getWeeklyRankingKey() {
        LocalDate now = LocalDate.now();
        int weekOfYear = now.get(WeekFields.of(Locale.KOREA).weekOfWeekBasedYear());
        return "ranking:weekly:" + now.getYear() + "-W" + String.format("%02d", weekOfYear);
    }

}
