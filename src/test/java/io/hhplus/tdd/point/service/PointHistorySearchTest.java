package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointHistorySearchTest {
    // 포인트 내역 조회

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    @DisplayName("사용자의 포인트 충전/이용 내역을 조회한다.")
    @Test
    void searchUserPointHistoryWhenUserIdInput() {
        long userId = 1L;
        when(pointHistoryRepository.getUserHistories(userId))
                .thenReturn(List.of());

        List<PointHistory> userPointHistories = pointService.getUserPointHistories(userId);

        assertThat(userPointHistories)
                .isNotNull()
                .isEmpty();
    }
}
