package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MyBatis 分页查询账单时返回的内部结果，包含关联查询得到的分类名称。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillQueryResult {

    /** 账单 ID。 */
    private Long id;

    /** 收支类型：EXPENSE 或 INCOME。 */
    private String transactionType;

    /** 分类 ID。 */
    private Long categoryId;

    /** 关联 categories 表查询得到的分类名称。 */
    private String categoryName;

    /** 账单金额。 */
    private BigDecimal amount;

    /** 记账日期。 */
    private LocalDate transactionDate;

    /** 备注，可为空。 */
    private String remark;

    /** 心情标签，可为空。 */
    private String mood;

    /** 创建时间，数据库 DATETIME 不包含时区。 */
    private LocalDateTime createdAt;

    /** 最近更新时间，数据库 DATETIME 不包含时区。 */
    private LocalDateTime updatedAt;
}
