package io.hhplus.tdd.point.service.unit;

import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.concurrent.PointLock;
import io.hhplus.tdd.point.concurrent.PointLockManager;
import io.hhplus.tdd.point.constraint.PointValidator;
import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Mock
    private PointValidator pointValidator;

    @Mock
    private PointLock pointLock;

    @Mock
    private PointLockManager lockManager;

    @InjectMocks
    private PointService pointService;

    @DisplayName("해당 유저의 포인트가 충분하지 않으면 PointErrorCode INSUFFICIENT_POINT_AMOUNT 반환한다.")
    @Test
    void throwExceptionWhenNotFoundUserPoint() throws InterruptedException {
        long userId = 1;
        long amount = 100;
        long currentPoint = 50;

        // lock 모킹
        when(lockManager.getLock(anyString())).thenReturn(pointLock);
        when(pointLock.tryLock(10, TimeUnit.MILLISECONDS)).thenReturn(true);

        when(userPointRepository.getUserPoint(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));

        doThrow(new PointException(PointErrorCode.INSUFFICIENT_POINT_AMOUNT))
                .when(pointValidator).usePointCheck(amount, currentPoint);

        assertThatThrownBy(() -> pointService.usePoint(userId, amount))
                .isInstanceOf(PointException.class)
                .extracting(e -> ((PointException) e).getErrorResponse().code(),
                            e -> ((PointException) e).getErrorResponse().message())
                .containsExactly(PointErrorCode.INSUFFICIENT_POINT_AMOUNT.getErrorResponse().code(),
                                 PointErrorCode.INSUFFICIENT_POINT_AMOUNT.getErrorResponse().message());

    }

    @DisplayName("사용하려는 포인트가 0이하일 경우 PointErrorCode INVALID_POINT_AMOUNT 반환한다")
    @Test
    void throwExceptionWhenUsingPointGreaterThanCurrentPoint() throws InterruptedException {
        long userId = 1;
        long currentPoint = 100;
        long amount = 200;
        long negativeAmount = -10;

        // lock 모킹
        when(lockManager.getLock(anyString())).thenReturn(pointLock);
        when(pointLock.tryLock(10, TimeUnit.MILLISECONDS)).thenReturn(true);

        when(userPointRepository.getUserPoint(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));

        doThrow(new PointException(PointErrorCode.INVALID_POINT_AMOUNT))
                .when(pointValidator).usePointCheck(negativeAmount, currentPoint);

        assertThatThrownBy(() -> pointService.usePoint(userId, negativeAmount))
                .isInstanceOf(PointException.class)
                .extracting(e -> ((PointException) e).getErrorResponse().code(),
                            e -> ((PointException) e).getErrorResponse().message())
                .containsExactly(PointErrorCode.INVALID_POINT_AMOUNT.getErrorResponse().code(),
                                 PointErrorCode.INVALID_POINT_AMOUNT.getErrorResponse().message());

    }

    @DisplayName("정상적으로 포인트를 사용했을 때 남은 포인트가 일치해야한다.")
    @Test
    void equalRemainPointWhenUsingPointIsNormal() throws InterruptedException {
        long userId = 1;
        long currentPoint = 500;
        long amount = 200;
        long remainPoint = 300;

        // lock 모킹
        when(lockManager.getLock(anyString())).thenReturn(pointLock);
        when(pointLock.tryLock(10, TimeUnit.MILLISECONDS)).thenReturn(true);

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
    void createPointUsingHistory() throws InterruptedException {
        long userId = 1;
        long currentPoint = 500;
        long amount = 200;
        long remainPoint = 300;

        // lock 모킹
        when(lockManager.getLock(anyString())).thenReturn(pointLock);
        when(pointLock.tryLock(10, TimeUnit.MILLISECONDS)).thenReturn(true);

        when(userPointRepository.getUserPoint(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));
        when(userPointRepository.createOrUpdate(userId, remainPoint))
                .thenReturn(new UserPoint(userId, remainPoint, System.currentTimeMillis()));

        pointService.usePoint(userId, amount);

        // 사용 기록 생성 호출
        verify(pointHistoryRepository).createPointHistory(eq(userId), eq(amount), eq(TransactionType.USE), anyLong());
    }



}
