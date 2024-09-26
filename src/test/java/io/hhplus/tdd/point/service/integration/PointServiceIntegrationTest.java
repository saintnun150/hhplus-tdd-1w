package io.hhplus.tdd.point.service.integration;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceIntegrationTest.class);

    @Autowired
    private PointService pointService;

    @Nested
    class DefaultTest {
        @DisplayName("포인트 충전 및 포인트 내역을 조회한다.")
        @Test
        @DirtiesContext
        void searchPointByUserId() {
            long userId = 1L;
            UserPoint createPoint = pointService.chargePoint(userId, 100L);
            UserPoint userPoint = pointService.getUserPoint(userId);
            assertThat(userPoint.id()).isEqualTo(createPoint.id());
            assertThat(userPoint.point()).isEqualTo(createPoint.point());
        }

        @DisplayName("포인트 사용 및 포인트 히스토리 내역을 조회한다.")
        @Test
        @DirtiesContext
        void searchPointHistoryByUserId() {
            long userId = 1L;
            pointService.chargePoint(userId, 100L);
            pointService.usePoint(1L, 50L);
            List<PointHistory> histories = pointService.getUserPointHistories(1L);
            assertThat(histories).hasSize(2);
        }
    }

    @DisplayName("포인트 충전, 사용 동시성 테스트")
    @Nested
    class ConcurrentTest {
        // 동시에 여러번의 포인트 충전을 시도할 때 시도한 모든 충전금액만큼 충전된다.
        // 동시에 여러번의 포인트 사용할 때
        // 충전과 사용을 동시에 여러번 할 때

        @DisplayName("하나의 사용자에 대해 충전을 동시에 시도할 때 시도한 만큼 충전된다.")
        @Test
        @DirtiesContext
        void shouldCreditAllAmountsWhenUserAttemptsSimultaneousRecharges() throws InterruptedException {
            long userId = 1;
            long chargePoint = 100;
            int threadCnt = 30;

            // 충전
            executeConcurrentTasks(threadCnt, () -> pointService.chargePoint(userId, chargePoint));

            UserPoint userPoint = pointService.getUserPoint(1);

            assertThat(userPoint.point()).
                    isEqualTo(chargePoint * threadCnt);

            // 포인트 히스토리 갯수 확인
            // 동시성 횟수
            List<PointHistory> histories = pointService.getUserPointHistories(userId);
            assertThat(histories).hasSize(threadCnt);
        }

        @DisplayName("하나의 사용자에 대해 보유한 포인트를 동시에 여러번 사용할 때 시도한 모든 포인트가 차감되어야한다.")
        @Test
        @DirtiesContext
        void shouldDeductAllAmountsWhenUserAttemptsMultipleSimultaneousPointUses() throws InterruptedException {
            long userId = 1;
            long amount = 10000;
            int threadCnt = 30;
            long usePoint = 30;

            // 충전
            UserPoint before = pointService.chargePoint(userId, amount);

            // 사용
            executeConcurrentTasks(threadCnt, () -> pointService.usePoint(userId, usePoint));

            UserPoint after = pointService.getUserPoint(userId);

            // 조회
            assertThat(after.point())
                    .isEqualTo(before.point() - usePoint * threadCnt);
        }

        @DisplayName("동시에 포인트 충전과 사용이 여러번 발생할 경우 시도만큼 포인트가 정상적으로 있어야 한다.")
        @Test
        @DirtiesContext
        void shouldMaintainCorrectPointBalanceWhenSimultaneousChargesAndUsesOccur() throws InterruptedException {
            long userId = 1;
            long amount = 10000;
            long chargePoint = 50;
            long usePoint = 30;
            int threadCnt = 30;

            // 최초 충전
            UserPoint before = pointService.chargePoint(userId, amount);

            // 2대 1 비율로 사용과 충전이 발생
            AtomicInteger count = new AtomicInteger(0);
            executeConcurrentTasks(threadCnt, () -> {
                if (count.incrementAndGet() % 3 == 0) {
                    // 사용
                    pointService.usePoint(userId, usePoint);
                } else {
                    // 충전
                    pointService.chargePoint(userId, chargePoint);
                }
            });

            UserPoint after = pointService.getUserPoint(userId);

            int expectUseCount = threadCnt / 3;
            int expectChargeCount = threadCnt - expectUseCount;

            assertThat(after.point())
                    .isEqualTo(before.point() + (chargePoint * expectChargeCount) - (usePoint * expectUseCount));

            List<PointHistory> histories = pointService.getUserPointHistories(userId);

            // 포인트 히스토리 갯수 확인
            // 최초 삽입 + 동시성 횟수
            assertThat(histories).hasSize(threadCnt + 1);
        }
    }

    private void executeConcurrentTasks(int threadCnt, Runnable task) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCnt);
        CountDownLatch latch = new CountDownLatch(threadCnt);

        for(int i = 0; i < threadCnt; ++i) {
            executorService.submit(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("## executor thread error[{}]", e.getMessage(), e);
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
    }
}
