package com.xiaoya.accounting.service.impl;

import com.xiaoya.accounting.dto.ExpenseCategoryQueryResult;
import com.xiaoya.accounting.dto.ExpenseCategoryStatisticsResponse;
import com.xiaoya.accounting.dto.MonthlyStatisticsQueryResult;
import com.xiaoya.accounting.dto.MonthlyStatisticsResponse;
import com.xiaoya.accounting.exception.InvalidParameterException;
import com.xiaoya.accounting.mapper.StatisticsMapper;
import com.xiaoya.accounting.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 月度统计业务实现类，负责汇总并组装统计响应。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    /** 统计数据访问对象，由构造器注入。 */
    private final StatisticsMapper statisticsMapper;

    /**
     * 查询指定月份的收支统计，并计算结余和分类支出比例。
     */
    @Override
    public MonthlyStatisticsResponse getMonthlyStatistics(Long userId, String month) {
        // 将 YYYY-MM 转成月份对象，以计算数据库查询的日期范围。
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(month);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new InvalidParameterException("month 格式必须为 YYYY-MM");
        }

        // 查询区间包含月初，但不包含下个月月初。
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.plusMonths(1).atDay(1);

        // 查询当月收入、支出和账单总数。
        MonthlyStatisticsQueryResult summary =
                statisticsMapper.selectMonthlySummary(
                        userId,
                        startDate,
                        endDate
                );

        BigDecimal totalIncome = summary.getTotalIncome();
        BigDecimal totalExpense = summary.getTotalExpense();

        // 收入减支出得到当月结余。
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // 查询每个有支出的分类，并计算其占月支出的比例。
        List<ExpenseCategoryQueryResult> categoryResults =
                statisticsMapper.selectExpenseByCategory(
                        userId,
                        startDate,
                        endDate
                );

        List<ExpenseCategoryStatisticsResponse> categoryResponses =
                new ArrayList<>();

        for (ExpenseCategoryQueryResult result : categoryResults) {
            String ratio = totalExpense.compareTo(BigDecimal.ZERO) == 0
                    ? "0.0000"
                    : result.getAmount()
                    .divide(totalExpense, 4, RoundingMode.HALF_UP)
                    .toPlainString();

            categoryResponses.add(new ExpenseCategoryStatisticsResponse(
                    result.getCategoryId(),
                    result.getCategoryName(),
                    formatAmount(result.getAmount()),
                    ratio
            ));
        }

        log.debug("月度统计查询完成，userId={}, month={}, billCount={}",
                userId,
                month,
                summary.getBillCount());

        return new MonthlyStatisticsResponse(
                month,
                formatAmount(totalIncome),
                formatAmount(totalExpense),
                formatAmount(balance),
                summary.getBillCount(),
                categoryResponses
        );
    }

    /**
     * 将数据库金额格式化为接口约定的两位小数字符串。
     */
    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}