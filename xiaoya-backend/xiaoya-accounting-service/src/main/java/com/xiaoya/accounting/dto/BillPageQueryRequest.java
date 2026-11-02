package com.xiaoya.accounting.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 分页查询账单接口的查询参数。
 */
@Data
public class BillPageQueryRequest {

    /** 月份筛选，可选，格式为 YYYY-MM。 */
    @Pattern(
            regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
            message = "月份格式必须为 YYYY-MM"
    )
    private String month;

    /** 收支类型筛选，可选，只能是 EXPENSE 或 INCOME。 */
    @Pattern(
            regexp = "^(EXPENSE|INCOME)$",
            message = "收支类型只能是 EXPENSE 或 INCOME"
    )
    private String transactionType;

    /** 分类 ID 筛选，可选；传入时必须大于 0。 */
    @Positive(message = "分类 ID 必须大于 0")
    private Long categoryId;

    /** 页码从 1 开始；未传时默认查询第 1 页。 */
    @Min(value = 1, message = "页码必须大于或等于 1")
    private Integer page = 1;

    /** 每页条数默认 20，最大 100。 */
    @Min(value = 1, message = "每页条数必须大于或等于 1")
    @Max(value = 100, message = "每页条数不能大于 100")
    private Integer pageSize = 20;
}
