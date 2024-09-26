package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.concurrent.PointLock;
import io.hhplus.tdd.point.concurrent.PointLockManager;
import io.hhplus.tdd.point.constraint.PointValidator;
import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    private final PointValidator pointValidator;
    private final PointLockManager lockManager;

    public UserPoint chargePoint(long id, long amount) {
        String lockKey = "point-" + id;
        PointLock lock = lockManager.getLock(lockKey);
        try {
            // 충전 정책 체크
            pointValidator.chargePointCheck(amount);
            if (lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                pointHistoryRepository.createPointHistory(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

                UserPoint userPoint = userPointRepository.getUserPoint(id);
                return userPointRepository.createOrUpdate(id, userPoint.point() + amount);
            }
            throw new PointException(PointErrorCode.LOCK_NOT_ACQUIRED);
        } catch (InterruptedException e) {
            throw new PointException(PointErrorCode.ILLEGAL_ACCESS_STATE);
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }

    }

    public UserPoint usePoint(long id, long amount) {
        String lockKey = "point-" + id;
        PointLock lock = lockManager.getLock(lockKey);
        try {
            if (lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                UserPoint userPoint = userPointRepository.getUserPoint(id);
                // 사용 정책 체크
                pointValidator.usePointCheck(amount, userPoint.point());
                pointHistoryRepository.createPointHistory(id, amount, TransactionType.USE, System.currentTimeMillis());
                return userPointRepository.createOrUpdate(id, userPoint.point() - amount);
            }
            throw new PointException(PointErrorCode.LOCK_NOT_ACQUIRED);
        } catch (InterruptedException e) {
            throw new PointException(PointErrorCode.ILLEGAL_ACCESS_STATE);
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }

    }

    public UserPoint getUserPoint(long id) {
        return userPointRepository.getUserPoint(id);
    }

    public List<PointHistory> getUserPointHistories(long id) {
        return pointHistoryRepository.getUserHistories(id);
    }
}
