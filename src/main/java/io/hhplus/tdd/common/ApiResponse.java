package io.hhplus.tdd.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {
    private final int status;
    private final T data;

    public ApiResponse(final int status, final T data) {
        this.status = status;
        this.data = data;
    }

    public static <T> ApiResponse<T> create(final T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), data);
    }
}
