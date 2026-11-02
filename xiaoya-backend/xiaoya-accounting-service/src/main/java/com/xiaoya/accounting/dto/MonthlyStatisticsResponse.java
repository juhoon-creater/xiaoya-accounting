package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 指定月份的收入、支出、结余及分类支出统计。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatisticsResponse {

    /** 查询月份，格式为 YYYY-MM。 */
    private String month;

    /** 当月收入合计，以字符串返回并保留两位小数。 */
    private String totalIncome;

    /** 当月支出合计，以字符串返回并保留两位小数。 */
    private String totalExpense;

    /** 当月结余，即收入减去支出。 */
    private String balance;

    /** 当月账单总数。 */
    private Long billCount;

    /** 当月有支出的分类统计。 */
    private List<ExpenseCategoryStatisticsResponse> expenseByCategory;
}