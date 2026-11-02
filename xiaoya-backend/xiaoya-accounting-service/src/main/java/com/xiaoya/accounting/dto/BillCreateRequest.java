package com.xiaoya.accounting.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 新增账单接口接收的请求数据。
 * 只负责承接和校验前端传来的字段，不在这里处理数据库或业务逻辑。
 */
@Data
public class BillCreateRequest {

    /** 收支类型，只能是 EXPENSE 或 INCOME。 */
    @NotBlank(message = "收支类型不能为空")
    @Pattern(regexp = "^(EXPENSE|INCOME)$", message = "收支类型只能是 EXPENSE 或 INCOME")
    private String transactionType;

    /** 系统分类 ID，必须是正整数。 */
    @NotNull(message = "分类 ID 不能为空")
    @Positive(message = "分类 ID 必须大于 0")
    private Long categoryId;

    /** 金额按接口约定以字符串接收，且大于 0、最多两位小数。 */
    @NotBlank(message = "金额不能为空")
    @Pattern(
            regexp = "^(0|[1-9][0-9]{0,9})(\\.[0-9]{1,2})?$",
            message = "金额格式不正确，最多保留两位小数"
    )
    @DecimalMin(value = "0.00", inclusive = false, message = "金额必须大于 0")
    private String amount;

    /** 记账日期；JSON 中使用 YYYY-MM-DD，转换为 LocalDate。 */
    @NotNull(message = "记账日期不能为空")
    private LocalDate transactionDate;

    /** 备注可选，最多 500 个字符。 */
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

    /** 心情标签可选；未选择时允许不传。 */
    private String mood;
}
