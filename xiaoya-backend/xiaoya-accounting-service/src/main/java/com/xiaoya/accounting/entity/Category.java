package com.xiaoya.accounting.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类实体，对应记账数据库中的 categories 表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    /**
     * 分类主键。
     */
    private Long id;

    /**
     * 分类显示名称，例如“餐饮”。
     */
    private String name;

    /**
     * 收支类型：EXPENSE 表示支出，INCOME 表示收入。
     */
    private String type;

    /**
     * 分类显示顺序。
     */
    private Integer sortOrder;

    /**
     * 分类是否启用。
     */
    private Boolean active;

    /**
     * 分类记录的创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 分类记录的最近更新时间。
     */
    private LocalDateTime updatedAt;
}
