package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 指定月份的总预算和分类预算查询结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetMonthResponse {

    /** 查询月份，格式为 YYYY-MM。 */
    private String month;

    /** 所有分类预算金额之和。 */
    private String totalBudgetAmount;

    /** 当月全部支出账单合计。 */
    private String totalUsedAmount;

    /** 总预算减去总支出；超支时为负数。 */
    private String totalRemainingAmount;

    /** 总预算使用比例；总预算为零时返回 "0"。 */
    private String totalUsageRatio;

    /** 各启用支出分类的预算和使用情况。 */
    private List<CategoryBudgetResponse> categories;
}