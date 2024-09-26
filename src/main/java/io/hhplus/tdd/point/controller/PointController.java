package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.common.ApiResponse;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.PointDto;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public ApiResponse<UserPoint> point(
            @PathVariable long id
    ) {
        return ApiResponse.create(pointService.getUserPoint(id));
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public ApiResponse<List<PointHistory>> history(
            @PathVariable long id
    ) {
        return ApiResponse.create(pointService.getUserPointHistories(id));
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public ApiResponse<UserPoint> charge(
            @PathVariable long id,
            @RequestBody PointDto.ChargeRequest dto) {
        return ApiResponse.create(pointService.chargePoint(id, dto.amount()));
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public  ApiResponse<UserPoint> use(
            @PathVariable long id,
            @RequestBody PointDto.UseRequest dto
    ) {
        return ApiResponse.create(pointService.usePoint(id, dto.amount()));
    }
}
