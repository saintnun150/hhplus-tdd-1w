package io.hhplus.tdd.point.concurrent;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class PointLockManager {
    private final ConcurrentHashMap<String, PointLock> lockMap = new ConcurrentHashMap<>();

    public <T> T executeWithLock(String lockId, Supplier<T> action) {
        PointLock pointLock = lockMap.computeIfAbsent(lockId, key -> new PointLock());
        pointLock.lock();
        try {
            return action.get();
        } finally {
            if (pointLock.isHeldByCurrentThread()) {
                pointLock.unlock();
            }
        }
    }
}
