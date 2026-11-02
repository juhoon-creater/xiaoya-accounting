package com.xiaoya.accounting.service;

import com.xiaoya.accounting.entity.Category;

import java.util.List;

/**
 * 分类业务接口。
 */
public interface CategoryService {

    /**
     * 根据收支类型查询分类列表。
     *
     * @param type 收支类型：EXPENSE 或 INCOME
     * @return 分类列表
     */
    List<Category> listByType(String type);
}