package io.hhplus.tdd.point.service.unit;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointSearchTest {

    @Mock
    private UserPointRepository userPointRepository;

    @InjectMocks
    private PointService pointService;

    @DisplayName("사용자의 포인트를 조회한다.")
    @Test
    void searchUserPointWhenUserIdInput() {
        long userId = 1L;
        when(userPointRepository.getUserPoint(userId))
                .thenReturn(UserPoint.empty(userId));

        UserPoint userPoint = pointService.getUserPoint(userId);

        assertThat(userPoint)
                .isNotNull()
                .extracting(UserPoint::id, UserPoint::point)
                .containsExactly(userId, 0L);
    }
}
