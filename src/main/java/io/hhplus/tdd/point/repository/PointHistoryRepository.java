package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;

import java.util.List;

public interface PointHistoryRepository {
    PointHistory createPointHistory(long userId, long amount, TransactionType type, long updateMillis);
    List<PointHistory> getUserHistories(long userId);
}
