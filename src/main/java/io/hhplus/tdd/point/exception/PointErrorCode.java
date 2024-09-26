package io.hhplus.tdd.point.exception;

import io.hhplus.tdd.common.ErrorResponse;
import io.hhplus.tdd.common.ErrorCode;

public enum PointErrorCode implements ErrorCode {
    INVALID_POINT_AMOUNT("ERR_001", "포인트 입력 값은 0보다 커야합니다."),
    INSUFFICIENT_POINT_AMOUNT("ERR_002", "잔여 포인트가 부족합니다."),
    CHARGE_POINT_LIMIT("ERR_003", "충전 1회 당 최대 10000포인트까지 가능합니다.");

    private final String code;
    private final String message;

    PointErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(code, message);
    }
}
