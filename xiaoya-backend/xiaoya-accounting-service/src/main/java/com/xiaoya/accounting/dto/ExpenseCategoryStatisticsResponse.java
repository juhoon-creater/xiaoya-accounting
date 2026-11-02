package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个分类的当月支出金额及其占比。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategoryStatisticsResponse {

    /** 系统分类 ID。 */
    private Long categoryId;

    /** 分类显示名称。 */
    private String categoryName;

    /** 该分类当月支出金额，保留两位小数。 */
    private String amount;

    /** 该分类支出占当月总支出的比例，保留四位小数。 */
    private String ratio;
}