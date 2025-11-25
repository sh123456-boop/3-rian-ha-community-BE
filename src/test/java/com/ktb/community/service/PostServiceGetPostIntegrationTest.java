//package com.ktb.community.service;
//
//import com.ktb.community.dto.response.PostResponseDto;
//import com.ktb.community.entity.Image;
//import com.ktb.community.entity.Post;
//import com.ktb.community.entity.PostImage;
//import com.ktb.community.entity.User;
//import com.ktb.community.repository.ImageRepository;
//import com.ktb.community.repository.PostRepository;
//import com.ktb.community.repository.UserLikePostsRepository;
//import com.ktb.community.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ZSetOperations;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.WeekFields;
//import java.util.Locale;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyDouble;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class PostServiceGetPostIntegrationTest {
//
//    @Autowired
//    private PostServiceImpl postService;
//
//    @Autowired
//    private PostRepository postRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ImageRepository imageRepository;
//
//    @Autowired
//    private UserLikePostsRepository userLikePostsRepository;
//
//    @MockBean
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @MockBean
//    private S3ServiceImpl s3Service;
//
//    @BeforeEach
//    void cleanDatabase() {
//        userLikePostsRepository.deleteAll();
//        postRepository.deleteAll();
//        imageRepository.deleteAll();
//        userRepository.deleteAll();
//    }
//
//    @Test
//    @DisplayName("게시글 단건 조회(getPost) 통합 테스트 - 기본 프로필 이미지와 조회수/랭킹 업데이트 검증")
//    void getPost_integrationTest_updatesViewAndRanking() {
//        // given
//        User author = User.builder()
//                .email("integration@test.com")
//                .password("strong-password")
//                .nickname("integrator")
//                .build();
//        User savedAuthor = userRepository.save(author);
//
//        Post post = Post.builder()
//                .title("Integration Title")
//                .contents("Integration Content")
//                .build();
//        post.setUser(savedAuthor);
//
//        Image postImageFile = Image.builder()
//                .s3Key("posts/integration-image.png")
//                .build();
//        PostImage postImage = PostImage.builder()
//                .post(post)
//                .image(postImageFile)
//                .orders(1)
//                .build();
//        post.setPostImageList(postImage);
//
//        Post savedPost = postRepository.save(post);
//
//        ZSetOperations<String, Object> zSetOperations = Mockito.mock(ZSetOperations.class);
//        Mockito.when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        Mockito.when(zSetOperations.incrementScore(anyString(), any(), anyDouble())).thenReturn(1.0);
//        Mockito.when(redisTemplate.getExpire(anyString())).thenReturn(-1L);
//        Mockito.when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
//
//        String expectedDailyKey = "ranking:daily:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//        WeekFields weekFields = WeekFields.of(Locale.KOREA);
//        int weekOfYear = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
//        String expectedWeeklyKey = "ranking:weekly:" + LocalDate.now().getYear() + "-W" + String.format("%02d", weekOfYear);
//
//        // when
//        PostResponseDto response = postService.getPost(savedPost.getId(), savedAuthor.getId());
//
//        // then
//        assertThat(response.getPostId()).isEqualTo(savedPost.getId());
//        assertThat(response.getTitle()).isEqualTo("Integration Title");
//        assertThat(response.getContent()).isEqualTo("Integration Content");
//        assertThat(response.getNickname()).isEqualTo("integrator");
//        assertThat(response.getUserId()).isEqualTo(savedAuthor.getId());
//        assertThat(response.isLikedByUser()).isFalse();
//
//        assertThat(response.getAuthorProfileImageUrl())
//                .isEqualTo("https://test.cloudfront.net/default.png");
//        assertThat(response.getImages()).hasSize(1);
//        PostResponseDto.ImageInfo imageInfo = response.getImages().get(0);
//        assertThat(imageInfo.getImageUrl()).isEqualTo("https://test.cloudfront.net/posts/integration-image.png");
//
//        Post refreshedPost = postRepository.findById(savedPost.getId()).orElseThrow();
//        assertThat(refreshedPost.getPostCount().getView_cnt()).isEqualTo(1);
//        assertThat(response.getViewCount()).isEqualTo(1);
//
//        verify(redisTemplate, times(1)).opsForZSet();
//        verify(zSetOperations, times(1)).incrementScore(expectedDailyKey, savedPost.getId().toString(), 1.0);
//        verify(zSetOperations, times(1)).incrementScore(expectedWeeklyKey, savedPost.getId().toString(), 1.0);
//        verify(redisTemplate, times(1)).getExpire(expectedDailyKey);
//        verify(redisTemplate, times(1)).expire(expectedDailyKey, 2L, TimeUnit.DAYS);
//        verify(redisTemplate, times(1)).getExpire(expectedWeeklyKey);
//        verify(redisTemplate, times(1)).expire(expectedWeeklyKey, 8L, TimeUnit.DAYS);
//    }
//
//    @Test
//    @DisplayName("게시글 단건 조회(getPost) 통합 테스트 - 멀티스레드 환경에서도 조회수와 랭킹이 정확히 누적")
//    void getPost_integrationTest_updatesViewAndRanking_concurrently() throws InterruptedException {
//        // given 테스트 데이터 (작성자/게시글/이미지)
//        User author = User.builder()
//                .email("concurrency@test.com")
//                .password("strong-password")
//                .nickname("parallel")
//                .build();
//        User savedAuthor = userRepository.save(author);
//
//        Post post = Post.builder()
//                .title("Concurrent Title")
//                .contents("Concurrent Content")
//                .build();
//        post.setUser(savedAuthor);
//
//        Image postImageFile = Image.builder()
//                .s3Key("posts/concurrent-image.png")
//                .build();
//        PostImage postImage = PostImage.builder()
//                .post(post)
//                .image(postImageFile)
//                .orders(1)
//                .build();
//        post.setPostImageList(postImage);
//
//        Post savedPost = postRepository.save(post);
//
//        // given: Redis 모킹(일간/주간 랭킹 갱신 + ttl 설정)
//        ZSetOperations<String, Object> zSetOperations = Mockito.mock(ZSetOperations.class);
//        Mockito.when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        Mockito.when(zSetOperations.incrementScore(anyString(), any(), anyDouble())).thenReturn(1.0);
//
//        // ttl이 설정되지 않은 경우로 가정
//        Mockito.when(redisTemplate.getExpire(anyString())).thenReturn(-1L);
//        Mockito.when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
//
//
//        // 일간/주간 랭킹 키 기대값
//        String expectedDailyKey = "ranking:daily:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//        WeekFields weekFields = WeekFields.of(Locale.KOREA);
//        int weekOfYear = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
//        String expectedWeeklyKey = "ranking:weekly:" + LocalDate.now().getYear() + "-W" + String.format("%02d", weekOfYear);
//
//
//        // 동시 실행을 위한 동기화 장치
//        int threadCount = 10; // 동시에 수행할 요청 수
//        CountDownLatch readyLatch = new CountDownLatch(threadCount); //  모든 스레드 준비 신호 수집
//        CountDownLatch startLatch = new CountDownLatch(1); // 동시에 시작하기 위한 게이트
//        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 모든 스레드 종료 대기
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount); // 고정 스레드 풀
//
//        // when
//        for (int i = 0; i < threadCount; i++) {
//            executor.submit(() -> {
//                try {
//                    readyLatch.countDown(); // 준비 완료 알림
//                    startLatch.await(); // 시작 신호 대기
//                    postService.getPost(savedPost.getId(), savedAuthor.getId()); // 실제 테스트 대상 메서드 호출
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                } finally {
//                    doneLatch.countDown(); // 작업 완료 알림
//                }
//            });
//        }
//
//        // 모든 스레드 준비가 끝날때까지 대기 -> 동시 시작의 전제 확보
//        readyLatch.await();
//        // 게이트 오픈 시각 기록
//        long startNanos = System.nanoTime();
//        startLatch.countDown(); // 게이트 오픈
//        // 모든 스레드 작업이 끝날 때까지 최대 10초 대기(타임아웃 방어)
//        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
//        // 테스트 종료 후 즉시 스레드 정리(블로킹 방지)
//        executor.shutdownNow();
//        // 경과시간(ms) 계산 및 출력
//        double elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000.0;
//        System.out.printf("[ConcurrentExecution] getPost elapsed: %.3f ms%n", elapsedMillis);
//
//        // then
//        // 모든 작업이 제시간 내에 완료되었는지 검증
//        assertThat(finished).isTrue();
//
//        //  ---------- then: DB 조회수 누적 검증 ----------
//        //  모든 호출 이후, view_cnt == threadCount 여야 동시성 환경에서도 정확히 누적된 것
//        Post refreshedPost = postRepository.findById(savedPost.getId()).orElseThrow();
//        assertThat(refreshedPost.getPostCount().getView_cnt()).isEqualTo(threadCount);
//
//        //  ---------- then: Redis 랭킹 반영 검증 ----------
//        //  Redis의 일간/주간 랭킹 반영도 threadCount 만큼 호출되었는지 검증
//        verify(redisTemplate, times(threadCount)).opsForZSet();
//
//        // 일간/주간 랭킹 점수 증가 호출 검증
//        verify(zSetOperations, times(threadCount)).incrementScore(expectedDailyKey, savedPost.getId().toString(), 1.0);
//        verify(zSetOperations, times(threadCount)).incrementScore(expectedWeeklyKey, savedPost.getId().toString(), 1.0);
//        // ttl 설정 호출 검증
//        // 각 호출에서 만료 미설정(-1L) → 매번 expire(...) 설정이 수행되어야 함
//        verify(redisTemplate, times(threadCount)).getExpire(expectedDailyKey);
//        verify(redisTemplate, times(threadCount)).expire(expectedDailyKey, 2L, TimeUnit.DAYS);
//        verify(redisTemplate, times(threadCount)).getExpire(expectedWeeklyKey);
//        verify(redisTemplate, times(threadCount)).expire(expectedWeeklyKey, 8L, TimeUnit.DAYS);
//    }
//
//}
