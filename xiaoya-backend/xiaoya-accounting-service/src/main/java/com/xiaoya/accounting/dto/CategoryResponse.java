package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类接口的响应数据，只包含接口文档规定的字段。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    /** 分类 ID。 */
    private Long id;

    /** 分类名称。 */
    private String name;

    /** 分类类型：EXPENSE 或 INCOME。 */
    private String type;

    /** 前端显示顺序。 */
    private Integer sortOrder;
}