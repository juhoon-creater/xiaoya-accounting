package com.xiaoya.accounting.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 设置分类预算接口接收的请求数据。
 */
@Data
public class CategoryBudgetSetRequest {

    /** 预算金额按接口约定以字符串传入，必须大于 0，最多两位小数。 */
    @NotBlank(message = "预算金额不能为空")
    @Pattern(
            regexp = "^(0|[1-9][0-9]{0,9})(\\.[0-9]{1,2})?$",
            message = "预算金额格式不正确，最多保留两位小数"
    )
    @DecimalMin(value = "0.00", inclusive = false, message = "预算金额必须大于 0")
    private String amount;
}