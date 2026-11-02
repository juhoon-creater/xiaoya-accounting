package com.xiaoya.accounting.service;

import com.xiaoya.accounting.dto.MonthlyStatisticsResponse;

/**
 * 统计业务接口，声明统计相关的业务操作。
 */
public interface StatisticsService {

    /**
     * 查询当前登录用户指定月份的收支统计。
     *
     * @param userId 当前登录用户 ID，由已验证的身份信息取得
     * @param month 统计月份，格式为 YYYY-MM
     * @return 月收入、月支出、结余和分类支出统计
     */
    MonthlyStatisticsResponse getMonthlyStatistics(Long userId, String month);
}