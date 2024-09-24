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
class PointServiceTest {

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    @DisplayName("충전 금액이 0또는 음수일 경우 IllegalArgumentException 발생")
    @Test
    void throwException_when_illegal_point_amount_params() {
        long id = 1L;
        long zeroPoint = 0;
        long negativePoint = -100;

        assertThatThrownBy(() -> pointService.chargePoint(id, zeroPoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount must be greater than 0");

        assertThatThrownBy(() -> pointService.chargePoint(id, negativePoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount must be greater than 0");
    }

    @DisplayName("신규 유저의 포인트를 충전")
    @Test
    void chargeTestById() {
        long id = 1L;
        long point = 100L;

        UserPoint expectUserPoint = new UserPoint(id, point, System.currentTimeMillis());

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
    void chargeTestByRId() {
        long id = 1L;
        long currentPoint = 100L;
        long extraChargeAmount = 200L;

        UserPoint alreadyUserPoint = new UserPoint(id, currentPoint, System.currentTimeMillis());

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
    void createPointChargeHistory() {
        long id = 1L;
        long currentPoint = 100L;
        long extraChargeAmount = 200L;

        when(userPointRepository.getUserPoint(id))
                .thenReturn(new UserPoint(id, currentPoint, System.currentTimeMillis()));

        pointService.chargePoint(id, extraChargeAmount);

        // 충전 기록 호출 확인
        verify(pointHistoryRepository).createPointHistory(eq(id), eq(extraChargeAmount), eq(TransactionType.CHARGE), anyLong());
    }



}