package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint chargePoint(long id, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        UserPoint userPoint = userPointTable.selectById(id);
        return userPointTable.insertOrUpdate(id, userPoint.point() + amount);
    }
}
