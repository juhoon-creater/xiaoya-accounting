package com.xiaoya.accounting.mapper;

import com.xiaoya.accounting.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 按收支类型查询所有已启用的分类。
     *
     * @param type 收支类型：EXPENSE 或 INCOME
     * @return 按显示顺序排列的分类列表
     */
    List<Category> selectByType(@Param("type") String type);
    /**
     * 根据 ID 查询已启用的分类。
     *
     * @param id 分类 ID
     * @return 找到时返回分类；不存在或已停用时返回 null
     */
    Category selectActiveById(@Param("id") Long id);
}

