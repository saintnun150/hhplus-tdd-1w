package io.hhplus.tdd.point.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PointDto {

    public record ChargeRequest(long amount) { }
    public record UseRequest(long amount) { }
}
