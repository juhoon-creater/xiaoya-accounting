package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mapper 查询当月收支汇总时使用的内部结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatisticsQueryResult {

    /** 当月收入合计。 */
    private BigDecimal totalIncome;

    /** 当月支出合计。 */
    private BigDecimal totalExpense;

    /** 当月账单总数。 */
    private Long billCount;
}