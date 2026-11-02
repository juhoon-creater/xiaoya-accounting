package com.xiaoya.accounting.mapper;

import com.xiaoya.accounting.dto.ExpenseCategoryQueryResult;
import com.xiaoya.accounting.dto.MonthlyStatisticsQueryResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 月度统计数据访问接口，SQL 写在 resources/mapper/StatisticsMapper.xml 中。
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 查询当前用户指定月份的收入、支出和账单数量。
     *
     * @param userId 当前登录用户 ID
     * @param startDate 月初，包含
     * @param endDate 下个月月初，不包含
     * @return 当月收支汇总；没有账单时金额和数量为 0
     */
    MonthlyStatisticsQueryResult selectMonthlySummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询当前用户指定月份每个有支出的分类金额。
     *
     * @param userId 当前登录用户 ID
     * @param startDate 月初，包含
     * @param endDate 下个月月初，不包含
     * @return 按支出金额降序排列的分类统计
     */
    List<ExpenseCategoryQueryResult> selectExpenseByCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
