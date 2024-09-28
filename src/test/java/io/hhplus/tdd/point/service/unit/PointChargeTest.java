package io.hhplus.tdd.point.service.unit;

import io.hhplus.tdd.point.value.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
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

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointChargeTest {

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private PointValidator pointValidator;

    @Mock
    private PointLockManager lockManager;

    @InjectMocks
    private PointService pointService;

    @DisplayName("충전 금액이 0또는 음수일 경우 PointException 발생")
    @Test
    void throwException_when_illegal_point_amount_params() {
        long id = 1L;
        long zeroPoint = 0;
        long negativePoint = -100;
        long limitedPoint = 20000;

        doThrow(new PointException(PointErrorCode.INVALID_POINT_AMOUNT))
                .when(pointValidator).chargePointCheck(zeroPoint);

        doThrow(new PointException(PointErrorCode.INVALID_POINT_AMOUNT))
                .when(pointValidator).chargePointCheck(negativePoint);

        doThrow(new PointException(PointErrorCode.CHARGE_POINT_LIMIT))
                .when(pointValidator).chargePointCheck(limitedPoint);

        assertThatThrownBy(() -> pointService.chargePoint(id, zeroPoint))
                .isInstanceOf(PointException.class)
                .extracting(e -> ((PointException) e).getErrorResponse().code(),
                            e -> ((PointException) e).getErrorResponse().message())
                .containsExactly(PointErrorCode.INVALID_POINT_AMOUNT.getErrorResponse().code(),
                                 PointErrorCode.INVALID_POINT_AMOUNT.getErrorResponse().message());

        assertThatThrownBy(() -> pointService.chargePoint(id, negativePoint))
                .isInstanceOf(PointException.class)
                .extracting(e -> ((PointException) e).getErrorResponse().code(),
                            e -> ((PointException) e).getErrorResponse().message())
                .containsExactly(PointErrorCode.INVALID_POINT_AMOUNT.getErrorResponse().code(),
                                 PointErrorCode.INVALID_POINT_AMOUNT.getErrorResponse().message());

        assertThatThrownBy(() -> pointService.chargePoint(id, limitedPoint))
                .isInstanceOf(PointException.class)
                .extracting(e -> ((PointException) e).getErrorResponse().code(),
                            e -> ((PointException) e).getErrorResponse().message())
                .containsExactly(PointErrorCode.CHARGE_POINT_LIMIT.getErrorResponse().code(),
                                 PointErrorCode.CHARGE_POINT_LIMIT.getErrorResponse().message());
    }

    @DisplayName("신규 유저의 포인트를 충전")
    @Test
    void chargeTestById() throws InterruptedException {
        long id = 1L;
        long point = 100L;

        UserPoint expectUserPoint = new UserPoint(id, point, System.currentTimeMillis());

        // lock 모킹
        when(lockManager.executeWithLock(anyString(), any()))
                .thenAnswer(answer -> {
                    Supplier<UserPoint> action = answer.getArgument(1);
                    return action.get();
                });

        when(userPointRepository.getUserPoint(id))
                .thenReturn(UserPoint.empty(id));
        when(userPointRepository.createOrUpdate(id, point))
                .thenReturn(expectUserPoint);

        UserPoint actual = pointService.chargePoint(id, point);

        assertThat(actual)
                .isEqualTo(expectUserPoint);
    }

    @DisplayName("이미 존재하는 유저의 포인트를 충전")
    @Test
    void chargeTestByRId() throws InterruptedException {
        long id = 1L;
        long currentPoint = 100L;
        long extraChargeAmount = 200L;

        UserPoint alreadyUserPoint = new UserPoint(id, currentPoint, System.currentTimeMillis());

        // lock 모킹
        when(lockManager.executeWithLock(anyString(), any()))
                .thenAnswer(answer -> {
                    Supplier<UserPoint> action = answer.getArgument(1);
                    return action.get();
                });

        long totalAmount = currentPoint + extraChargeAmount;

        when(userPointRepository.getUserPoint(id))
                .thenReturn(alreadyUserPoint);
        when(userPointRepository.createOrUpdate(id, totalAmount))
                .thenReturn(new UserPoint(id, totalAmount, System.currentTimeMillis()));

        UserPoint actual = pointService.chargePoint(id, extraChargeAmount);

        assertThat(actual)
                .extracting(UserPoint::id, UserPoint::point)
                .containsExactly(id, totalAmount);
    }

    @DisplayName("포인트를 충전할 때 충전 기록을 생성")
    @Test
    void createPointChargeHistory() throws InterruptedException {
        long id = 1L;
        long currentPoint = 100L;
        long extraChargeAmount = 200L;

        // lock 모킹
        when(lockManager.executeWithLock(anyString(), any()))
                .thenAnswer(answer -> {
                    Supplier<UserPoint> action = answer.getArgument(1);
                    return action.get();
                });

        when(userPointRepository.getUserPoint(id))
                .thenReturn(new UserPoint(id, currentPoint, System.currentTimeMillis()));

        pointService.chargePoint(id, extraChargeAmount);

        // 충전 기록 호출 확인
        verify(pointHistoryRepository).createPointHistory(eq(id), eq(extraChargeAmount), eq(TransactionType.CHARGE), anyLong());
    }
}