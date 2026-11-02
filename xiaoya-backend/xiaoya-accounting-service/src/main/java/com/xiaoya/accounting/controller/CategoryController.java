package com.xiaoya.accounting.controller;

import com.xiaoya.accounting.common.ApiResponse;
import com.xiaoya.accounting.dto.CategoryResponse;
import com.xiaoya.accounting.entity.Category;
import com.xiaoya.accounting.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类接口控制器，负责接收分类相关的 HTTP 请求。
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    /**
     * 分类业务对象，由构造器注入。
     */
    private final CategoryService categoryService;

    /**
     * 根据收支类型查询已启用的分类。
     *
     * @param type 收支类型：EXPENSE 或 INCOME
     * @return 分类列表
     */
    @GetMapping
    public ApiResponse<List<CategoryResponse>> listByType(
            @RequestParam(value = "type", required = false) String type) {

        List<Category> categories = categoryService.listByType(type);

        List<CategoryResponse> response = categories.stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getType(),
                        category.getSortOrder()))
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }
    }
