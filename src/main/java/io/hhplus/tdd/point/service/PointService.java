package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
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

    public UserPoint chargePoint(long id, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        pointHistoryRepository.createPointHistory(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        UserPoint userPoint = userPointRepository.getUserPoint(id);
        return userPointRepository.createOrUpdate(id, userPoint.point() + amount);
    }

    public UserPoint usePoint(long id, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        UserPoint userPoint = userPointRepository.getUserPoint(id);
        if (userPoint.point() == 0) {
            throw new IllegalStateException("current point is zero");
        }
        if (userPoint.point() < amount) {
            throw new IllegalStateException("current point is less than amount");
        }

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
