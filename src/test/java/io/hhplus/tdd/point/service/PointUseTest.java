package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointUseTest {
    // 현재 보유 포인트는 0보다 커야한다.
    // 사용하려는 포인트가 현재 보유 포인트를 초과할 경우 사용 불가
    // 사용하려는 포인트는 0원보다 커야한다.
    // 정상적으로 포인트를 사용했을 때 남은 포인트가 일치해야한다.
    // 포인트를 사용할 경우 포인트 이용 내역이 기록되어야 한다.

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;


    @DisplayName("해당 유저의 포인트가 존재하지 않으면 IllegalStateException을 반환한다.")
    @Test
    void throwExceptionWhenNotFoundUserPoint() {
        long userId = 1;
        long amount = 100;

        when(userPointRepository.getUserPoint(userId))
                .thenReturn(UserPoint.empty(userId));

        assertThatThrownBy(() -> pointService.usePoint(userId, amount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("zero");

    }

    @DisplayName("사용하려는 포인트가 현재 보유 포인트를 초과할 경우 IllegalStateException을 반환한다.")
    @Test
    void throwExceptionWhenUsingPointGreaterThanCurrentPoint() {
        long userId = 1;
        long currentPoint = 100;
        long amount = 200;

        when(userPointRepository.getUserPoint(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));

        assertThatThrownBy(() -> pointService.usePoint(userId, amount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("current point is less than amount");

    }

    @DisplayName("사용하려는 포인트가 0보다 작거나 같을 경우 IllegalArgumentException을 반환한다.")
    @Test
    void throwExceptionWhenUsingPointLeZero() {
        long userId = 1;
        long amount = -100;

        assertThatThrownBy(() -> pointService.usePoint(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount must be greater than 0");

    }

    @DisplayName("정상적으로 포인트를 사용했을 때 남은 포인트가 일치해야한다.")
    @Test
    void equalRemainPointWhenUsingPointIsNormal() {
        long userId = 1;
        long currentPoint = 500;
        long amount = 200;
        long remainPoint = 300;

        when(userPointRepository.getUserPoint(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));
        when(userPointRepository.createOrUpdate(userId, remainPoint))
                .thenReturn(new UserPoint(userId, remainPoint, System.currentTimeMillis()));

        UserPoint updatedUserPoint = pointService.usePoint(userId, amount);

        assertThat(updatedUserPoint)
                .extracting(UserPoint::point)
                .isEqualTo(remainPoint);
    }

    @DisplayName("포인트를 사용할 경우 포인트 이용 내역이 기록되어야 한다.")
    @Test
    void createPointUsingHistory() {
        long userId = 1;
        long currentPoint = 500;
        long amount = 200;
        long remainPoint = 300;

        when(userPointRepository.getUserPoint(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));
        when(userPointRepository.createOrUpdate(userId, remainPoint))
                .thenReturn(new UserPoint(userId, remainPoint, System.currentTimeMillis()));

        pointService.usePoint(userId, amount);

        // 사용 기록 생성 호출
        verify(pointHistoryRepository).createPointHistory(eq(userId), eq(amount), eq(TransactionType.USE), anyLong());
    }



}
