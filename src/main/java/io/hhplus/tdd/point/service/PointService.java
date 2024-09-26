package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.constraint.PointValidator;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    private final PointValidator pointValidator;

    public UserPoint chargePoint(long id, long amount) {
        // 충전 정책 체크
        pointValidator.chargePointCheck(amount);

        pointHistoryRepository.createPointHistory(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        UserPoint userPoint = userPointRepository.getUserPoint(id);
        return userPointRepository.createOrUpdate(id, userPoint.point() + amount);
    }

    public UserPoint usePoint(long id, long amount) {
        UserPoint userPoint = userPointRepository.getUserPoint(id);
        // 사용 정책 체크
        pointValidator.usePointCheck(amount, userPoint.point());
        pointHistoryRepository.createPointHistory(id, amount, TransactionType.USE, System.currentTimeMillis());
        return userPointRepository.createOrUpdate(id, userPoint.point() - amount);
    }

    public UserPoint getUserPoint(long id) {
        return userPointRepository.getUserPoint(id);
    }

    public List<PointHistory> getUserPointHistories(long id) {
        return pointHistoryRepository.getUserHistories(id);
    }
}
