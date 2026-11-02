package com.xiaoya.accounting.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单实体，对应记账数据库中的 bills 表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    /** 账单主键。 */
    private Long id;

    /** 所属用户 ID，由已验证的 Token 确定，不由请求体传入。 */
    private Long userId;

    /** 系统分类 ID。 */
    private Long categoryId;

    /** 收支类型：EXPENSE 表示支出，INCOME 表示收入。 */
    private String transactionType;

    /** 账单金额；数据库使用 DECIMAL(12, 2)，所以 Java 使用 BigDecimal。 */
    private BigDecimal amount;

    /** 实际记账日期。 */
    private LocalDate transactionDate;

    /** 账单备注，可为空。 */
    private String remark;

    /** 心情标签，可为空。 */
    private String mood;

    /** 账单创建时间。 */
    private LocalDateTime createdAt;

    /** 账单最近更新时间。 */
    private LocalDateTime updatedAt;
}
