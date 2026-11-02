package com.xiaoya.accounting.service.impl;

import com.xiaoya.accounting.entity.Category;
import com.xiaoya.accounting.exception.InvalidParameterException;
import com.xiaoya.accounting.mapper.CategoryMapper;
import com.xiaoya.accounting.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    /**
     * 分类数据访问对象，由构造器注入。
     */
    private final CategoryMapper categoryMapper;

    /**
     * 调用 Mapper 查询指定收支类型的分类。
     */
    @Override
    public List<Category> listByType(String type) {
        if (type != null
                && !type.equals("EXPENSE")
                && !type.equals("INCOME")) {
            throw new InvalidParameterException(
                    "type 只能是 EXPENSE 或 INCOME"
            );
        }

        List<Category> categories = categoryMapper.selectByType(type);

// 分类查询属于常规读取，使用 DEBUG，避免生产环境 INFO 日志过多。
        log.debug("分类查询完成，type={}, count={}",
                type == null ? "ALL" : type,
                categories.size());

        return categories;
    }
}
