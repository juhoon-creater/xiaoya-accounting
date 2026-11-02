package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个支出分类的预算和使用情况。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBudgetResponse {

    /** 系统分类 ID。 */
    private Long categoryId;

    /** 分类名称。 */
    private String categoryName;

    /** 分类预算金额；未设置时返回 "0.00"。 */
    private String budgetAmount;

    /** 该分类当月支出合计。 */
    private String usedAmount;

    /** 分类预算减去已用金额；超支时为负数。 */
    private String remainingAmount;

    /** 分类预算使用比例；预算为零时为 null。 */
    private String usageRatio;
}