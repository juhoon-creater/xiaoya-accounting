package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mapper 查询单个分类的当月支出汇总时使用的内部结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategoryQueryResult {

    /** 系统分类 ID。 */
    private Long categoryId;

    /** 分类名称。 */
    private String categoryName;

    /** 该分类当月支出合计。 */
    private BigDecimal amount;
}