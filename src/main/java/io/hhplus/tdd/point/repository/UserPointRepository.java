package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.domain.UserPoint;

public interface UserPointRepository {
    UserPoint getUserPoint(long userId);
    UserPoint createOrUpdate(long id, long amount);
}
