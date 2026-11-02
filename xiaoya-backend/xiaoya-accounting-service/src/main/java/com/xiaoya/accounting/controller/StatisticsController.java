package com.xiaoya.accounting.controller;

import com.xiaoya.accounting.common.ApiResponse;
import com.xiaoya.accounting.dto.MonthlyStatisticsResponse;
import com.xiaoya.accounting.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 月度统计接口控制器，负责接收统计相关的 HTTP 请求。
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    /** 统计业务对象，由构造器注入。 */
    private final StatisticsService statisticsService;

    /**
     * 查询当前登录用户指定月份的收支统计。
     *
     * @param userId Gateway 验证 Token 后传入的当前用户 ID
     * @param month 查询月份，格式为 YYYY-MM
     * @return HTTP 200 和月度统计数据
     */
    @GetMapping("/monthly")
    public ApiResponse<MonthlyStatisticsResponse> getMonthlyStatistics(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "month", required = false) String month) {

        MonthlyStatisticsResponse response =
                statisticsService.getMonthlyStatistics(userId, month);

        return ApiResponse.success(response);
    }
}