package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mapper 查询单个分类预算和账单汇总时使用的内部结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetCategoryQueryResult {

    /** 系统分类 ID。 */
    private Long categoryId;

    /** 分类名称。 */
    private String categoryName;

    /** 分类预算；尚未设置时，数据库查询结果可能为 null。 */
    private BigDecimal budgetAmount;

    /** 该分类当月支出合计。 */
    private BigDecimal usedAmount;
}