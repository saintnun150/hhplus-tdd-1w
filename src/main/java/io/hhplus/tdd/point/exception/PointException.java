package io.hhplus.tdd.point.exception;

import io.hhplus.tdd.common.ErrorResponse;
import lombok.Getter;

@Getter
public class PointException extends RuntimeException{
    private final ErrorResponse errorResponse;

    public PointException(PointErrorCode pointErrorCode) {
        super(pointErrorCode.getErrorResponse().message());
        this.errorResponse = pointErrorCode.getErrorResponse();
    }

}
