package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
