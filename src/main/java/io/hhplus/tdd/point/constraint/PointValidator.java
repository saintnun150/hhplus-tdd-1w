package io.hhplus.tdd.point.constraint;

import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import org.springframework.stereotype.Component;

@Component
public class PointValidator {
    public void chargePointCheck(long amount) {
        if (amount <= 0) {
            throw new PointException(PointErrorCode.INVALID_POINT_AMOUNT);
        }
        if (amount > 10000) {
            throw new PointException(PointErrorCode.CHARGE_POINT_LIMIT);
        }
    }

    public void usePointCheck(long amount, long currentPoint) {
        if (amount <= 0) {
            throw new PointException(PointErrorCode.INVALID_POINT_AMOUNT);
        }
        if (currentPoint < amount) {
            throw new PointException(PointErrorCode.INSUFFICIENT_POINT_AMOUNT);
        }
    }
}
