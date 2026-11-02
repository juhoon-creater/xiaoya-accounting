package com.xiaoya.accounting.service;

import com.xiaoya.accounting.dto.BudgetMonthResponse;
import com.xiaoya.accounting.dto.CategoryBudgetSetRequest;

/**
 * 预算业务接口，声明预算相关的业务操作。
 */
public interface BudgetService {

    /**
     * 查询当前登录用户指定月份的预算和使用情况。
     *
     * @param userId 当前登录用户 ID，由已验证的身份信息取得
     * @param month 查询月份，格式为 YYYY-MM
     * @return 包含总预算、总支出和分类预算使用情况的响应数据
     */
    BudgetMonthResponse getMonthlyBudget(Long userId, String month);
    /**
     * 设置或修改当前用户指定月份某个支出分类的预算。
     *
     * @param userId 当前登录用户 ID，由已验证的身份信息取得
     * @param month 预算月份，格式为 YYYY-MM
     * @param categoryId 要设置预算的分类 ID
     * @param request 包含预算金额的请求数据
     * @return 更新后的整个月预算和使用情况
     */
    BudgetMonthResponse setCategoryBudget(
            Long userId,
            String month,
            Long categoryId,
            CategoryBudgetSetRequest request
    );
    /**
     * 清除当前用户指定月份某个支出分类的预算额度。
     *
     * @param userId 当前登录用户 ID，由已验证的身份信息取得
     * @param month 预算月份，格式为 YYYY-MM
     * @param categoryId 要清除预算的分类 ID
     * @return 清除后更新的整个月预算和使用情况
     */
    BudgetMonthResponse clearCategoryBudget(
            Long userId,
            String month,
            Long categoryId
    );
}
