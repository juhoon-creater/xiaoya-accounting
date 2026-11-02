package com.xiaoya.accounting.mapper;

import com.xiaoya.accounting.dto.BudgetCategoryQueryResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 预算数据访问接口，对应 SQL 写在 resources/mapper/BudgetMapper.xml 中。
 */
@Mapper
public interface BudgetMapper {


    /**
     * 查询当前用户指定月份所有启用的支出分类及其预算、已用金额。
     *
     * @param userId 当前登录用户 ID
     * @param budgetMonth 该月第一天，用于查月预算主记录
     * @param startDate 当月第一天，包含
     * @param endDate 下个月第一天，不包含
     * @return 每个启用支出分类的预算和支出汇总
     */
    List<BudgetCategoryQueryResult> selectCategoryBudgetUsage(
            @Param("userId") Long userId,
            @Param("budgetMonth") LocalDate budgetMonth,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询该用户该月保存的分类预算总额。
     *
     * @param userId 当前登录用户 ID
     * @param budgetMonth 该月第一天
     * @return 总预算；尚未创建该月预算时数据库可能返回 null
     */
    BigDecimal selectTotalBudgetAmount(
            @Param("userId") Long userId,
            @Param("budgetMonth") LocalDate budgetMonth
    );

    /**
     * 汇总当前用户指定月份的全部支出账单。
     *
     * @param userId 当前登录用户 ID
     * @param startDate 当月第一天，包含
     * @param endDate 下个月第一天，不包含
     * @return 当月支出总额；没有支出账单时 SQL 应返回 0
     */
    BigDecimal selectTotalUsedAmount(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    /**
     * 确保当前用户指定月份的月预算主记录存在。
     *
     * @param userId 当前登录用户 ID
     * @param budgetMonth 该月第一天
     * @return 数据库受影响行数
     */
    int insertMonthlyBudgetIfAbsent(
            @Param("userId") Long userId,
            @Param("budgetMonth") LocalDate budgetMonth
    );

    /**
     * 查询当前用户指定月份的月预算主记录 ID。
     *
     * @param userId 当前登录用户 ID
     * @param budgetMonth 该月第一天
     * @return 月预算主记录 ID
     */
    Long selectMonthlyBudgetId(
            @Param("userId") Long userId,
            @Param("budgetMonth") LocalDate budgetMonth
    );

    /**
     * 插入分类预算；如果该分类预算已存在，则更新其金额。
     *
     * @param monthlyBudgetId 月预算主记录 ID
     * @param categoryId 支出分类 ID
     * @param amount 分类预算金额
     * @return 数据库受影响行数
     */
    int upsertCategoryBudget(
            @Param("monthlyBudgetId") Long monthlyBudgetId,
            @Param("categoryId") Long categoryId,
            @Param("amount") BigDecimal amount
    );

    /**
     * 根据分类预算明细重新汇总月预算主表的总金额。
     *
     * @param monthlyBudgetId 月预算主记录 ID
     * @return 数据库受影响行数
     */
    int updateMonthlyTotalAmount(
            @Param("monthlyBudgetId") Long monthlyBudgetId
    );
    /**
     * 删除指定月预算下某个分类的预算明细。
     *
     * @param monthlyBudgetId 月预算主记录 ID
     * @param categoryId 要清除预算的分类 ID
     * @return 删除的记录数；没有该预算明细时返回 0
     */
    int deleteCategoryBudget(
            @Param("monthlyBudgetId") Long monthlyBudgetId,
            @Param("categoryId") Long categoryId
    );
}