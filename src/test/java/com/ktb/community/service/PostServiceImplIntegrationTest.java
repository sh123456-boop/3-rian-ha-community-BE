package com.ktb.community.service;

import com.ktb.community.dto.response.PostSliceResponseDto;
import com.ktb.community.entity.Image;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.User;
import com.ktb.community.repository.ImageRepository;
import com.ktb.community.repository.PostRepository;
import com.ktb.community.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // 위에서 만든 application-test.yml 설정을 사용
class PostServiceImplIntegrationTest {

    @Autowired
    private PostServiceImpl postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private static final int PAGE_SIZE = 10;
    private static final int DATASET_SIZE = 500;

    private TransactionTemplate readOnlyTxTemplate;

    @BeforeEach
    void setUp() {
        // 각 테스트 실행 전 데이터를 모두 삭제하여 격리성 보장
        // deleteAllInBatch는 연관 엔티티를 남겨둘 수 있어 순차 삭제로 정리
        postRepository.deleteAll();
        imageRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 데이터 생성 (500개의 게시글)
        for (int i = 1; i <= DATASET_SIZE; i++) {
            User user = User.builder()
                    .email("user" + i + "@test.com")
                    .password("password123")   // 유효한 길이의 비밀번호
                    .nickname("u" + i)   // 2~10자 제약을 만족하는 닉네임
                    .build();

            Image image = Image.builder()
                    .s3Key("profile-" + i)
                    .user(user)
                    .build();
            user.updateProfileImage(image);
            User savedUser = userRepository.save(user);

            Post post = Post.builder().title("Title " + i).contents("Content " + i).user(savedUser).build();
            postRepository.save(post);
        }

        readOnlyTxTemplate = new TransactionTemplate(transactionManager);
        readOnlyTxTemplate.setReadOnly(true);
        readOnlyTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }
    

    @Test
    @DisplayName("게시글 목록 조회(getPostSlice) 동시성 통합 테스트")
    void getPostSlice_concurrencyIntegrationTest() throws InterruptedException {
        // given
        int threadCount = 100; // 동시에 요청을 보낼 스레드 수

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<PostSliceResponseDto> results = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 실제 서비스 메서드 호출 (DB 조회 발생)
                    PostSliceResponseDto result = postService.getPostSliceForNPlusOneTest(null);
                    results.add(result);
                } catch (Exception e) {
                    exceptions.add(e); // 예외가 발생하면 기록
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 작업을 완료할 때까지 대기
        executorService.shutdown();

        // then
        // 1. 테스트 동안 예외가 발생하지 않았는지 확인
        assertThat(exceptions).isEmpty();

        // 2. 모든 스레드가 결과를 정상적으로 받았는지 확인
        assertThat(results.size()).isEqualTo(threadCount);

        // 3. 모든 스레드가 받은 결과의 내용이 일관되는지 확인
        PostSliceResponseDto firstResult = results.get(0);
        assertThat(firstResult.getPosts()).hasSize(PAGE_SIZE);
        assertThat(firstResult.isHasNext()).isTrue();
        // 첫 번째 게시물의 제목이 가장 최근 게시글인지 확인 (ID 내림차순이므로)
        assertThat(firstResult.getPosts().get(0).getTitle()).isEqualTo("Title " + DATASET_SIZE);
    }

    @Test
    @DisplayName("Fetch Join 유무에 따른 단일 스레드 실행 시간 비교")
    void compareExecutionTime_singleThread() {
        // warm-up to stabilize first-run overhead
        measureExecutionMillis(() -> postService.getPostSliceForNPlusOneTest(null));
        measureExecutionMillis(() -> postService.getPostSlice(null));

        int iterations = 5;
        List<Double> withoutFetchTimings = new ArrayList<>();
        List<Double> withFetchTimings = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            withoutFetchTimings.add(measureExecutionMillis(() -> postService.getPostSliceForNPlusOneTest(null)));
            withFetchTimings.add(measureExecutionMillis(() -> postService.getPostSlice(null)));
        }

        double withoutFetchAverage = withoutFetchTimings.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        double withFetchAverage = withFetchTimings.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        System.out.printf("[ExecutionTime] without fetch join: %.3f ms, with fetch join: %.3f ms%n",
                withoutFetchAverage, withFetchAverage);

        assertThat(withoutFetchTimings).hasSize(iterations);
        assertThat(withFetchTimings).hasSize(iterations);
    }

    private double measureExecutionMillis(Supplier<PostSliceResponseDto> supplier) {
        return readOnlyTxTemplate.execute(status -> {
            long start = System.nanoTime();
            PostSliceResponseDto dto = supplier.get();
            long elapsedNanos = System.nanoTime() - start;

            assertThat(dto.getPosts()).hasSize(PAGE_SIZE);
            assertThat(dto.isHasNext()).isTrue();

            return elapsedNanos / 1_000_000.0;
        });
    }

    @Test
    @DisplayName("Fetch Join 유무에 따른 멀티 스레드 실행 시간 비교")
    void compareExecutionTime_multiThreaded() throws InterruptedException {
        int threadCount = 50;
        int iterations = 3;

        // warm-up
        measureConcurrentExecutionMillis(() -> postService.getPostSliceForNPlusOneTest(null), 10);
        measureConcurrentExecutionMillis(() -> postService.getPostSlice(null), 10);

        List<Double> withoutFetchTimings = new ArrayList<>();
        List<Double> withFetchTimings = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            withoutFetchTimings.add(measureConcurrentExecutionMillis(() -> postService.getPostSliceForNPlusOneTest(null), threadCount));
            withFetchTimings.add(measureConcurrentExecutionMillis(() -> postService.getPostSlice(null), threadCount));
        }

        double withoutFetchAverage = withoutFetchTimings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double withFetchAverage = withFetchTimings.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        System.out.printf("[ParallelExecutionTime] without fetch join: %.3f ms, with fetch join: %.3f ms%n",
                withoutFetchAverage, withFetchAverage);

        assertThat(withoutFetchTimings).hasSize(iterations);
        assertThat(withFetchTimings).hasSize(iterations);
    }

    private double measureConcurrentExecutionMillis(Supplier<PostSliceResponseDto> supplier, int threadCount) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    readOnlyTxTemplate.execute(status -> {
                        PostSliceResponseDto dto = supplier.get();
                        assertThat(dto.getPosts()).hasSize(PAGE_SIZE);
                        assertThat(dto.isHasNext()).isTrue();
                        return null;
                    });
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(); // wait for all threads to be ready
        long start = System.nanoTime();
        startLatch.countDown(); // fire!
        doneLatch.await();
        long elapsedNanos = System.nanoTime() - start;

        executorService.shutdownNow();

        assertThat(errors).isEmpty();
        return elapsedNanos / 1_000_000.0;
    }
}
