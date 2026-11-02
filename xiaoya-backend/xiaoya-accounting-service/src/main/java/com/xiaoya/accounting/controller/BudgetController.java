package com.xiaoya.accounting.controller;

import com.xiaoya.accounting.common.ApiResponse;
import com.xiaoya.accounting.dto.BudgetMonthResponse;
import com.xiaoya.accounting.dto.CategoryBudgetSetRequest;
import com.xiaoya.accounting.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 预算接口控制器，负责接收预算相关的 HTTP 请求。
 */
@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    /** 预算业务对象，由构造器注入。 */
    private final BudgetService budgetService;

    /**
     * 查询当前登录用户指定月份的预算和使用情况。
     *
     * @param userId Gateway 验证 Token 后传入的当前用户 ID
     * @param month 路径中的查询月份，格式为 YYYY-MM
     * @return HTTP 200 和该月份预算数据
     */
    @GetMapping("/monthly/{month}")
    public ApiResponse<BudgetMonthResponse> getMonthlyBudget(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("month") String month) {

        BudgetMonthResponse response =
                budgetService.getMonthlyBudget(userId, month);

        return ApiResponse.success(response);
    }
    /**
     * 设置或修改当前登录用户指定月份某个支出分类的预算。
     *
     * @param userId Gateway 验证 Token 后传入的当前用户 ID
     * @param month 路径中的预算月份，格式为 YYYY-MM
     * @param categoryId 路径中的支出分类 ID
     * @param request 请求体中的预算金额
     * @return HTTP 200 和更新后的整个月预算
     */
    @PutMapping("/monthly/{month}/categories/{categoryId}")
    public ApiResponse<BudgetMonthResponse> setCategoryBudget(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("month") String month,
            @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody CategoryBudgetSetRequest request) {

        BudgetMonthResponse response = budgetService.setCategoryBudget(
                userId,
                month,
                categoryId,
                request
        );

        return ApiResponse.success(response);
    }
    /**
     * 清除当前登录用户指定月份某个支出分类的预算额度。
     *
     * @param userId Gateway 验证 Token 后传入的当前用户 ID
     * @param month 路径中的预算月份，格式为 YYYY-MM
     * @param categoryId 路径中的支出分类 ID
     * @return HTTP 200 和清除后的整个月预算
     */
    @DeleteMapping("/monthly/{month}/categories/{categoryId}")
    public ApiResponse<BudgetMonthResponse> clearCategoryBudget(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("month") String month,
            @PathVariable("categoryId") Long categoryId) {

        BudgetMonthResponse response =
                budgetService.clearCategoryBudget(
                        userId,
                        month,
                        categoryId
                );

        return ApiResponse.success(response);
    }
}