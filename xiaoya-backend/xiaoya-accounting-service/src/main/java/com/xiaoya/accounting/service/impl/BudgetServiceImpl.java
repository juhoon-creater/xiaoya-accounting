package com.xiaoya.accounting.service.impl;

import com.xiaoya.accounting.dto.BudgetCategoryQueryResult;
import com.xiaoya.accounting.dto.BudgetMonthResponse;
import com.xiaoya.accounting.dto.CategoryBudgetResponse;
import com.xiaoya.accounting.dto.CategoryBudgetSetRequest;
import com.xiaoya.accounting.entity.Category;
import com.xiaoya.accounting.exception.InvalidParameterException;
import com.xiaoya.accounting.mapper.BudgetMapper;
import com.xiaoya.accounting.mapper.CategoryMapper;
import com.xiaoya.accounting.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 预算业务实现类，负责查询预算并计算预算使用情况。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    /** 预算数据访问对象，由构造器注入。 */
    private final BudgetMapper budgetMapper;
    private final CategoryMapper categoryMapper;


    /**
     * 查询指定月份的预算、账单支出，并计算剩余金额和使用比例。
     */
    @Override
    public BudgetMonthResponse getMonthlyBudget(Long userId, String month) {
        // 把 YYYY-MM 转成月份对象，后续用它计算数据库查询日期范围。
        YearMonth yearMonth;
        try {
            if (month == null) {
                throw new DateTimeParseException("月份不能为空", "", 0);
            }
            yearMonth = YearMonth.parse(month);
        } catch (DateTimeParseException exception) {
            throw new InvalidParameterException("month 格式必须为 YYYY-MM");
        }

        // 查询区间包含月初，但不包含下个月月初。
        LocalDate budgetMonth = yearMonth.atDay(1);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.plusMonths(1).atDay(1);

        // 主表尚无该月预算记录时，总预算按 0 处理。
        BigDecimal totalBudget = budgetMapper.selectTotalBudgetAmount(
                userId,
                budgetMonth
        );
        if (totalBudget == null) {
            totalBudget = BigDecimal.ZERO;
        }

        // 统计当月全部支出账单。
        BigDecimal totalUsed = budgetMapper.selectTotalUsedAmount(
                userId,
                startDate,
                endDate
        );
        if (totalUsed == null) {
            totalUsed = BigDecimal.ZERO;
        }

        // 查询每个启用支出分类对应的预算和实际支出。
        List<BudgetCategoryQueryResult> queryResults =
                budgetMapper.selectCategoryBudgetUsage(
                        userId,
                        budgetMonth,
                        startDate,
                        endDate
                );

        List<CategoryBudgetResponse> categoryResponses = new ArrayList<>();

        for (BudgetCategoryQueryResult result : queryResults) {
            BigDecimal categoryBudget = result.getBudgetAmount();
            if (categoryBudget == null) {
                categoryBudget = BigDecimal.ZERO;
            }

            BigDecimal categoryUsed = result.getUsedAmount();
            if (categoryUsed == null) {
                categoryUsed = BigDecimal.ZERO;
            }

            BigDecimal categoryRemaining = categoryBudget.subtract(categoryUsed);

            // 分类预算为 0 时，接口约定使用比例返回 null。
            String categoryUsageRatio = categoryBudget.compareTo(BigDecimal.ZERO) == 0
                    ? null
                    : calculateRatio(categoryUsed, categoryBudget);

            categoryResponses.add(new CategoryBudgetResponse(
                    result.getCategoryId(),
                    result.getCategoryName(),
                    formatAmount(categoryBudget),
                    formatAmount(categoryUsed),
                    formatAmount(categoryRemaining),
                    categoryUsageRatio
            ));
        }

        BigDecimal totalRemaining = totalBudget.subtract(totalUsed);

        // 总预算为 0 时，接口约定使用比例返回字符串 "0"。
        String totalUsageRatio = totalBudget.compareTo(BigDecimal.ZERO) == 0
                ? "0"
                : calculateRatio(totalUsed, totalBudget);

        log.debug("预算查询完成，userId={}, month={}, categoryCount={}",
                userId,
                month,
                categoryResponses.size());

        return new BudgetMonthResponse(
                month,
                formatAmount(totalBudget),
                formatAmount(totalUsed),
                formatAmount(totalRemaining),
                totalUsageRatio,
                categoryResponses
        );
    }
    /**
     * 设置或修改分类预算，并重新计算该月总预算。
     */
    @Override
    @Transactional
    public BudgetMonthResponse setCategoryBudget(
            Long userId,
            String month,
            Long categoryId,
            CategoryBudgetSetRequest request) {

        // 先确认分类存在、启用，并且属于支出分类。

        Category category = categoryMapper.selectActiveById(categoryId);
        if (category == null || !"EXPENSE".equals(category.getType())) {
            throw new InvalidParameterException(
                    "categoryId 必须是已启用的支出分类"
            );
        }

        // 将接口传入的 YYYY-MM 转为月份对象；无效月份按参数错误处理。
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(month);
        } catch (DateTimeParseException exception) {
            throw new InvalidParameterException("month 格式必须为 YYYY-MM");
        }

        LocalDate budgetMonth = yearMonth.atDay(1);

        // 首次设置该月预算时先创建主记录；已存在时不会重复创建。
        budgetMapper.insertMonthlyBudgetIfAbsent(userId, budgetMonth);

        // 读取主记录 ID，供分类预算明细表关联使用。
        Long monthlyBudgetId =
                budgetMapper.selectMonthlyBudgetId(userId, budgetMonth);

        // 请求 DTO 已校验金额格式和范围，这里转为精确的 BigDecimal。
        BigDecimal amount = new BigDecimal(request.getAmount());

        // 同一月、同一分类再次设置时更新原额度，不新增重复记录。
        budgetMapper.upsertCategoryBudget(
                monthlyBudgetId,
                categoryId,
                amount
        );

        // 从所有分类预算明细重新求和，保证主表总预算与明细一致。
        budgetMapper.updateMonthlyTotalAmount(monthlyBudgetId);

        log.info("分类预算设置完成，userId={}, month={}, categoryId={}",
                userId,
                month,
                categoryId);

        // 返回更新后的整个月预算，响应结构与查询接口相同。
        return getMonthlyBudget(userId, month);
    }
    /**
     * 清除分类预算并重新计算该月总预算。
     */
    @Override
    @Transactional
    public BudgetMonthResponse clearCategoryBudget(
            Long userId,
            String month,
            Long categoryId) {

        // 只允许清除已启用的支出分类预算。
        Category category = categoryMapper.selectActiveById(categoryId);
        if (category == null || !"EXPENSE".equals(category.getType())) {
            throw new InvalidParameterException(
                    "categoryId 必须是已启用的支出分类"
            );
        }

        // 校验月份格式，并转换为数据库使用的该月第一天。
        YearMonth yearMonth;
        try {
            if (month == null) {
                throw new DateTimeParseException("月份不能为空", "", 0);
            }
            yearMonth = YearMonth.parse(month);
        } catch (DateTimeParseException exception) {
            throw new InvalidParameterException("month 格式必须为 YYYY-MM");
        }

        LocalDate budgetMonth = yearMonth.atDay(1);

        // 只查当前用户自己的月份记录，避免访问其他用户的预算。
        Long monthlyBudgetId =
                budgetMapper.selectMonthlyBudgetId(userId, budgetMonth);

        if (monthlyBudgetId != null) {
            // 删除分类额度；没有该明细时受影响行数为 0，仍按幂等成功处理。
            budgetMapper.deleteCategoryBudget(monthlyBudgetId, categoryId);

            // 以剩余分类额度重新计算总预算。
            budgetMapper.updateMonthlyTotalAmount(monthlyBudgetId);
        }

        log.info("分类预算清除完成，userId={}, month={}, categoryId={}",
                userId,
                month,
                categoryId);

        // 账单没有删除；重新查询会保留该分类的实际支出金额。
        return getMonthlyBudget(userId, month);
    }

    /**
     * 计算已使用金额占预算的比例，保留四位小数。
     */
    private String calculateRatio(BigDecimal usedAmount, BigDecimal budgetAmount) {
        return usedAmount.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                .toPlainString();
    }

    /**
     * 将金额格式化为接口约定的两位小数字符串。
     */
    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}