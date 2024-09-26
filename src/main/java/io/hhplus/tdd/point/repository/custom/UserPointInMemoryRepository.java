package io.hhplus.tdd.point.repository.custom;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPointInMemoryRepository implements UserPointRepository {

    private final UserPointTable userPointTable;

    @Override
    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    @Override
    public UserPoint createOrUpdate(long id, long amount) {
        return userPointTable.insertOrUpdate(id, amount);
    }
}
