package com.ktb.community.service;

import com.ktb.community.dto.response.PostResponseDto;
import com.ktb.community.entity.Image;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.PostImage;
import com.ktb.community.entity.User;
import com.ktb.community.repository.ImageRepository;
import com.ktb.community.repository.PostRepository;
import com.ktb.community.repository.UserLikePostsRepository;
import com.ktb.community.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class PostServiceGetPostIntegrationTest {

    @Autowired
    private PostServiceImpl postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserLikePostsRepository userLikePostsRepository;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private S3ServiceImpl s3Service;

    @BeforeEach
    void cleanDatabase() {
        userLikePostsRepository.deleteAll();
        postRepository.deleteAll();
        imageRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 단건 조회(getPost) 통합 테스트 - 기본 프로필 이미지와 조회수/랭킹 업데이트 검증")
    void getPost_integrationTest_updatesViewAndRanking() {
        // given
        User author = User.builder()
                .email("integration@test.com")
                .password("strong-password")
                .nickname("integrator")
                .build();
        User savedAuthor = userRepository.save(author);

        Post post = Post.builder()
                .title("Integration Title")
                .contents("Integration Content")
                .build();
        post.setUser(savedAuthor);

        Image postImageFile = Image.builder()
                .s3Key("posts/integration-image.png")
                .build();
        PostImage postImage = PostImage.builder()
                .post(post)
                .image(postImageFile)
                .orders(1)
                .build();
        post.setPostImageList(postImage);

        Post savedPost = postRepository.save(post);

        ZSetOperations<String, Object> zSetOperations = Mockito.mock(ZSetOperations.class);
        Mockito.when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Mockito.when(zSetOperations.incrementScore(anyString(), any(), anyDouble())).thenReturn(1.0);
        Mockito.when(redisTemplate.getExpire(anyString())).thenReturn(-1L);
        Mockito.when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        String expectedDailyKey = "ranking:daily:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        WeekFields weekFields = WeekFields.of(Locale.KOREA);
        int weekOfYear = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
        String expectedWeeklyKey = "ranking:weekly:" + LocalDate.now().getYear() + "-W" + String.format("%02d", weekOfYear);

        // when
        PostResponseDto response = postService.getPost(savedPost.getId(), savedAuthor.getId());

        // then
        assertThat(response.getPostId()).isEqualTo(savedPost.getId());
        assertThat(response.getTitle()).isEqualTo("Integration Title");
        assertThat(response.getContent()).isEqualTo("Integration Content");
        assertThat(response.getNickname()).isEqualTo("integrator");
        assertThat(response.getUserId()).isEqualTo(savedAuthor.getId());
        assertThat(response.isLikedByUser()).isFalse();

        assertThat(response.getAuthorProfileImageUrl())
                .isEqualTo("https://test.cloudfront.net/default.png");
        assertThat(response.getImages()).hasSize(1);
        PostResponseDto.ImageInfo imageInfo = response.getImages().get(0);
        assertThat(imageInfo.getImageUrl()).isEqualTo("https://test.cloudfront.net/posts/integration-image.png");

        Post refreshedPost = postRepository.findById(savedPost.getId()).orElseThrow();
        assertThat(refreshedPost.getPostCount().getView_cnt()).isEqualTo(1);
        assertThat(response.getViewCount()).isEqualTo(1);

        verify(redisTemplate, times(1)).opsForZSet();
        verify(zSetOperations, times(1)).incrementScore(expectedDailyKey, savedPost.getId().toString(), 1.0);
        verify(zSetOperations, times(1)).incrementScore(expectedWeeklyKey, savedPost.getId().toString(), 1.0);
        verify(redisTemplate, times(1)).getExpire(expectedDailyKey);
        verify(redisTemplate, times(1)).expire(expectedDailyKey, 2L, TimeUnit.DAYS);
        verify(redisTemplate, times(1)).getExpire(expectedWeeklyKey);
        verify(redisTemplate, times(1)).expire(expectedWeeklyKey, 8L, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("게시글 단건 조회(getPost) 통합 테스트 - 멀티스레드 환경에서도 조회수와 랭킹이 정확히 누적")
    void getPost_integrationTest_updatesViewAndRanking_concurrently() throws InterruptedException {
        // given
        User author = User.builder()
                .email("concurrency@test.com")
                .password("strong-password")
                .nickname("parallel")
                .build();
        User savedAuthor = userRepository.save(author);

        Post post = Post.builder()
                .title("Concurrent Title")
                .contents("Concurrent Content")
                .build();
        post.setUser(savedAuthor);

        Image postImageFile = Image.builder()
                .s3Key("posts/concurrent-image.png")
                .build();
        PostImage postImage = PostImage.builder()
                .post(post)
                .image(postImageFile)
                .orders(1)
                .build();
        post.setPostImageList(postImage);

        Post savedPost = postRepository.save(post);

        ZSetOperations<String, Object> zSetOperations = Mockito.mock(ZSetOperations.class);
        Mockito.when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Mockito.when(zSetOperations.incrementScore(anyString(), any(), anyDouble())).thenReturn(1.0);
        Mockito.when(redisTemplate.getExpire(anyString())).thenReturn(-1L);
        Mockito.when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        String expectedDailyKey = "ranking:daily:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        WeekFields weekFields = WeekFields.of(Locale.KOREA);
        int weekOfYear = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
        String expectedWeeklyKey = "ranking:weekly:" + LocalDate.now().getYear() + "-W" + String.format("%02d", weekOfYear);

        int threadCount = 10;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    postService.getPost(savedPost.getId(), savedAuthor.getId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        long startNanos = System.nanoTime();
        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();
        double elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000.0;
        System.out.printf("[ConcurrentExecution] getPost elapsed: %.3f ms%n", elapsedMillis);

        // then
        assertThat(finished).isTrue();

        Post refreshedPost = postRepository.findById(savedPost.getId()).orElseThrow();
        assertThat(refreshedPost.getPostCount().getView_cnt()).isEqualTo(threadCount);

        verify(redisTemplate, times(threadCount)).opsForZSet();
        verify(zSetOperations, times(threadCount)).incrementScore(expectedDailyKey, savedPost.getId().toString(), 1.0);
        verify(zSetOperations, times(threadCount)).incrementScore(expectedWeeklyKey, savedPost.getId().toString(), 1.0);
        verify(redisTemplate, times(threadCount)).getExpire(expectedDailyKey);
        verify(redisTemplate, times(threadCount)).expire(expectedDailyKey, 2L, TimeUnit.DAYS);
        verify(redisTemplate, times(threadCount)).getExpire(expectedWeeklyKey);
        verify(redisTemplate, times(threadCount)).expire(expectedWeeklyKey, 8L, TimeUnit.DAYS);
    }

}
