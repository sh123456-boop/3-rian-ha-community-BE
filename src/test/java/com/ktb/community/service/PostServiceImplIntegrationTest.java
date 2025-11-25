//package com.ktb.community.service;
//
//import com.ktb.community.dto.response.PostSliceResponseDto;
//import com.ktb.community.entity.Image;
//import com.ktb.community.entity.Post;
//import com.ktb.community.entity.User;
//import com.ktb.community.repository.ImageRepository;
//import com.ktb.community.repository.PostRepository;
//import com.ktb.community.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.TransactionDefinition;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.function.Supplier;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@ActiveProfiles("test") // 위에서 만든 application-test.yml 설정을 사용
//class PostServiceImplIntegrationTest {
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
//    private PlatformTransactionManager transactionManager;
//
//    @MockBean
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @MockBean
//    private S3ServiceImpl s3Service;
//
//    private static final int PAGE_SIZE = 10;
//    private static final int DATASET_SIZE = 500;
//
//    private TransactionTemplate readOnlyTxTemplate;
//
//    @BeforeEach
//    void setUp() {
//        // 각 테스트 실행 전 데이터를 모두 삭제하여 격리성 보장
//        // deleteAllInBatch는 연관 엔티티를 남겨둘 수 있어 순차 삭제로 정리
//        postRepository.deleteAll();
//        imageRepository.deleteAll();
//        userRepository.deleteAll();
//
//        // 테스트 데이터 생성 (500개의 게시글)
//        for (int i = 1; i <= DATASET_SIZE; i++) {
//            User user = User.builder()
//                    .email("user" + i + "@test.com")
//                    .password("password123")   // 유효한 길이의 비밀번호
//                    .nickname("u" + i)   // 2~10자 제약을 만족하는 닉네임
//                    .build();
//
//            Image image = Image.builder()
//                    .s3Key("profile-" + i)
//                    .user(user)
//                    .build();
//            user.updateProfileImage(image);
//            User savedUser = userRepository.save(user);
//
//            Post post = Post.builder().title("Title " + i).contents("Content " + i).user(savedUser).build();
//            postRepository.save(post);
//        }
//
//        readOnlyTxTemplate = new TransactionTemplate(transactionManager);
//        readOnlyTxTemplate.setReadOnly(true);
//        readOnlyTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
//    }
//
//
//    @Test
//    @DisplayName("게시글 목록 조회(getPostSlice) 동시성 통합 테스트")
//    void getPostSlice_concurrencyIntegrationTest() throws InterruptedException {
//        // given
//        int threadCount = 100; // 동시에 요청을 보낼 스레드 수
//
//        // when
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//        List<PostSliceResponseDto> results = Collections.synchronizedList(new ArrayList<>());
//        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
//
//        for (int i = 0; i < threadCount; i++) {
//            executorService.submit(() -> {
//                try {
//                    // 실제 서비스 메서드 호출 (DB 조회 발생)
//                    PostSliceResponseDto result = postService.getPostSliceForNPlusOneTest(null);
//                    results.add(result);
//                } catch (Exception e) {
//                    exceptions.add(e); // 예외가 발생하면 기록
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(); // 모든 스레드가 작업을 완료할 때까지 대기
//        executorService.shutdown();
//
//        // then
//        // 1. 테스트 동안 예외가 발생하지 않았는지 확인
//        assertThat(exceptions).isEmpty();
//
//        // 2. 모든 스레드가 결과를 정상적으로 받았는지 확인
//        assertThat(results.size()).isEqualTo(threadCount);
//
//        // 3. 모든 스레드가 받은 결과의 내용이 일관되는지 확인
//        PostSliceResponseDto firstResult = results.get(0);
//        assertThat(firstResult.getPosts()).hasSize(PAGE_SIZE);
//        assertThat(firstResult.isHasNext()).isTrue();
//        // 첫 번째 게시물의 제목이 가장 최근 게시글인지 확인 (ID 내림차순이므로)
//        assertThat(firstResult.getPosts().get(0).getTitle()).isEqualTo("Title " + DATASET_SIZE);
//    }
//
//    @Test
//    @DisplayName("Fetch Join 유무에 따른 단일 스레드 실행 시간 비교")
//    void compareExecutionTime_singleThread() {
//        // warm-up to stabilize first-run overhead
//        measureExecutionMillis(() -> postService.getPostSliceForNPlusOneTest(null));
//        measureExecutionMillis(() -> postService.getPostSlice(null));
//
//        int iterations = 5;
//        List<Double> withoutFetchTimings = new ArrayList<>();
//        List<Double> withFetchTimings = new ArrayList<>();
//
//        for (int i = 0; i < iterations; i++) {
//            withoutFetchTimings.add(measureExecutionMillis(() -> postService.getPostSliceForNPlusOneTest(null)));
//            withFetchTimings.add(measureExecutionMillis(() -> postService.getPostSlice(null)));
//        }
//
//        double withoutFetchAverage = withoutFetchTimings.stream()
//                .mapToDouble(Double::doubleValue)
//                .average()
//                .orElse(0);
//        double withFetchAverage = withFetchTimings.stream()
//                .mapToDouble(Double::doubleValue)
//                .average()
//                .orElse(0);
//
//        System.out.printf("[ExecutionTime] without fetch join: %.3f ms, with fetch join: %.3f ms%n",
//                withoutFetchAverage, withFetchAverage);
//
//        assertThat(withoutFetchTimings).hasSize(iterations);
//        assertThat(withFetchTimings).hasSize(iterations);
//    }
//
//    private double measureExecutionMillis(Supplier<PostSliceResponseDto> supplier) {
//        return readOnlyTxTemplate.execute(status -> {
//            long start = System.nanoTime();
//            PostSliceResponseDto dto = supplier.get();
//            long elapsedNanos = System.nanoTime() - start;
//
//            assertThat(dto.getPosts()).hasSize(PAGE_SIZE);
//            assertThat(dto.isHasNext()).isTrue();
//
//            return elapsedNanos / 1_000_000.0;
//        });
//    }
//
//    @Test
//    @DisplayName("Fetch Join 유무에 따른 멀티 스레드 실행 시간 비교")
//    void compareExecutionTime_multiThreaded() throws InterruptedException {
//        int threadCount = 100; // 반복에 동작할 스레드 수(병렬성)
//        int iterations = 3; // 반복 측정 횟수(평균 내기 위함)
//
//        // warm-up : Jit/캐시 예열
//        // 본 측정 전에 소수의 스레드(10개)로 빠르게 예열하여 초기 오버헤드 제거
//        measureConcurrentExecutionMillis(() -> postService.getPostSliceForNPlusOneTest(null), 10);
//        measureConcurrentExecutionMillis(() -> postService.getPostSlice(null), 10);
//
//        // 결과 수집용 리스트 (각 반복의 ms 단위 측정값)
//        List<Double> withoutFetchTimings = new ArrayList<>();
//        List<Double> withFetchTimings = new ArrayList<>();
//
//        // --------------- measure : fetch 미적용 vs 적용을 번갈아 가며 동일한 조건을 측정 -----------------
//        for (int i = 0; i < iterations; i++) {
//            // N + 1 발생 서비스 로직을 threadCount 동시 실행으로 측정
//            withoutFetchTimings.add(measureConcurrentExecutionMillis(() -> postService.getPostSliceForNPlusOneTest(null), threadCount));
//            // fetch join 서비스 로직을 동일 조건으로 측정
//            withFetchTimings.add(measureConcurrentExecutionMillis(() -> postService.getPostSlice(null), threadCount));
//        }
//
//        // ----------- 평균 (ms) 계산 -------------
//        double withoutFetchAverage = withoutFetchTimings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
//        double withFetchAverage = withFetchTimings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
//
//        // 결과를 콘솔로 출력
//        // 예) [ParallelExecutionTime] without fetch join: 85.123 ms, with fetch join: 42.987 ms
//        System.out.printf("[ParallelExecutionTime] without fetch join: %.3f ms, with fetch join: %.3f ms%n",
//                withoutFetchAverage, withFetchAverage);
//
//        // 반복 횟수만큼 수집되었는지
//        assertThat(withoutFetchTimings).hasSize(iterations);
//        assertThat(withFetchTimings).hasSize(iterations);
//    }
//
//    private double measureConcurrentExecutionMillis(Supplier<PostSliceResponseDto> supplier, int threadCount) throws InterruptedException {
//        // threadCount 만큼 동시에 실행하라 고정 크기 스레드 풀
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        // 모든 스레드가 준비 될때까지 기다리는 래치
//        CountDownLatch readyLatch = new CountDownLatch(threadCount);
//        // 한 번에 동시에 시작시키기 위한 게이트 역할(1 -> 0 되는 순간 모든 대기 스레드 시작)
//        CountDownLatch startLatch = new CountDownLatch(1);
//        // 모든 스레드가 끝날 때까지 기다리는 래치
//        CountDownLatch doneLatch = new CountDownLatch(threadCount);
//        // 각 스레드에서 발생한 예외를 수집(테스트 실패 원인 파악)
//        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
//
//        // threadCount 개의 작업 제출
//        for (int i = 0; i < threadCount; i++) {
//            executorService.submit(() -> {
//                // 현재 스레드가 준비되었음을 알림(readyLatch 1 감소)
//                readyLatch.countDown();
//                try {
//                    // 모든 스레드가 준비될 때까지 대기 후, startLatch가 열릴 때 동시 출발
//                    startLatch.await();
//
//                    // 트랜젝션/세션 비용을 일정하게 하기 위해 readOnly 트랜잭션 내에서 서비스 호출
//                    readOnlyTxTemplate.execute(status -> {
//                        // 실제 비즈니스 로직 실행
//                        PostSliceResponseDto dto = supplier.get();
//
//                        // 슬라이스 결과의 정합성 검증(측정의 유효성 보장)
//                        assertThat(dto.getPosts()).hasSize(PAGE_SIZE);
//                        assertThat(dto.isHasNext()).isTrue();
//                        return null;
//                    });
//                } catch (Throwable t) {
//                    // 어떤 예외든 수집
//                    errors.add(t);
//                } finally {
//                    // 이 스레드의 작업 종료 알림
//                    doneLatch.countDown();
//                }
//            });
//        }
//
//        // 모든 스레드가 준비될 때까지 대기
//        readyLatch.await();
//        // 시작 시각 기록(게이트 오픈 직전)
//        long start = System.nanoTime();
//        // 게이트 오픈 : 대기 중인 모든 스레드가 동시에 supplier.get()을 수행
//        startLatch.countDown();
//        // 모든 스레드가 작업을 마칠 때까지 대기
//        doneLatch.await();
//        // 총 경과 시간 계산
//        long elapsedNanos = System.nanoTime() - start;
//
//        // 스레드 풀 즉시 종료 시도
//        executorService.shutdownNow();
//
//        // 실행 중 예외가 1건도 없어야 함(발생 시 테스트 실패)
//        assertThat(errors).isEmpty();
//
//        // ns -> ms로 변환하여 반환
//        return elapsedNanos / 1_000_000.0;
//    }
//}
