package io.hhplus.tdd.point.concurrent;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class PointLockManager {
    protected ConcurrentHashMap<String, PointLock> lockMap = new ConcurrentHashMap<>();

    public PointLock getLock(final String lockId) {
        PointLock pointLock = lockMap.computeIfAbsent(lockId, k -> new PointLock());
        return pointLock;

    }
}
