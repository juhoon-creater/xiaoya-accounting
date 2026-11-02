package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 新增账单成功后返回给客户端的数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillCreateResponse {

    /** 新创建的账单 ID。 */
    private Long id;

    /** 收支类型：EXPENSE 或 INCOME。 */
    private String transactionType;

    /** 系统分类 ID。 */
    private Long categoryId;

    /** 分类显示名称，来自 categories 表，不是 bills 表字段。 */
    private String categoryName;

    /** 金额以字符串形式返回，符合接口文档约定。 */
    private String amount;

    /** 实际记账日期。 */
    private LocalDate transactionDate;

    /** 备注；未填写时为 null。 */
    private String remark;

    /** 心情标签；未填写时为 null。 */
    private String mood;

    /** 创建时间，响应中包含时区偏移，例如 +08:00。 */
    private OffsetDateTime createdAt;

    /** 最近更新时间，响应中包含时区偏移，例如 +08:00。 */
    private OffsetDateTime updatedAt;
}
