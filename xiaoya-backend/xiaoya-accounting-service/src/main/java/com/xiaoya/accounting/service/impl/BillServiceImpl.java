package com.xiaoya.accounting.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaoya.accounting.dto.*;
import com.xiaoya.accounting.entity.Bill;
import com.xiaoya.accounting.entity.Category;
import com.xiaoya.accounting.exception.BillNotFoundException;
import com.xiaoya.accounting.exception.InvalidParameterException;
import com.xiaoya.accounting.mapper.BillMapper;
import com.xiaoya.accounting.mapper.CategoryMapper;
import com.xiaoya.accounting.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单业务实现类，负责校验并创建账单。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    /** 普通 MyBatis 账单数据访问接口。 */
    private final BillMapper billMapper;

    /** 普通 MyBatis 分类数据访问接口。 */
    private final CategoryMapper categoryMapper;

    /**
     * 校验分类后保存账单，并查询刚保存的记录组装响应。
     */
    @Override
    @Transactional
    public BillCreateResponse createBill(Long userId, BillCreateRequest request) {
        // 先确认用户选择的是一个存在且启用中的系统分类。
        Category category = categoryMapper.selectActiveById(request.getCategoryId());
        if (category == null) {
            throw new InvalidParameterException("分类不存在或已停用");
        }

        // 账单的收支类型必须与分类的收支类型一致。
        if (!request.getTransactionType().equals(category.getType())) {
            throw new InvalidParameterException("账单收支类型与分类类型不匹配");
        }

        // 将请求中的金额字符串转成适合金额运算的 BigDecimal。
        Bill bill = new Bill();
        bill.setUserId(userId);
        bill.setCategoryId(request.getCategoryId());
        bill.setTransactionType(request.getTransactionType());
        bill.setAmount(new BigDecimal(request.getAmount()));
        bill.setTransactionDate(request.getTransactionDate());
        bill.setRemark(request.getRemark());
        bill.setMood(request.getMood());

        // 插入后，MyBatis 会把数据库生成的账单 ID 写回 bill.id。
        int affectedRows = billMapper.insert(bill);
        if (affectedRows != 1) {
            throw new IllegalStateException("新增账单失败");
        }

        // 使用账单 ID 和当前用户 ID 查询，确保读取的是当前用户自己的账单。
        Bill savedBill = billMapper.selectByIdAndUserId(bill.getId(), userId);
        if (savedBill == null) {
            throw new IllegalStateException("账单已写入，但未能查询到新增记录");
        }

        // 数据库 DATETIME 不带时区，响应按接口文档使用上海时区偏移。
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        log.info(
                "账单创建成功，userId={}, billId={}, transactionType={}",
                userId,
                savedBill.getId(),
                savedBill.getTransactionType()
        );
        return new BillCreateResponse(
                savedBill.getId(),
                savedBill.getTransactionType(),
                savedBill.getCategoryId(),
                category.getName(),
                savedBill.getAmount().toPlainString(),
                savedBill.getTransactionDate(),
                savedBill.getRemark(),
                savedBill.getMood(),
                savedBill.getCreatedAt().atZone(zoneId).toOffsetDateTime(),
                savedBill.getUpdatedAt().atZone(zoneId).toOffsetDateTime()
        );
    }
    /**
     * 按当前用户、月份、收支类型和分类筛选账单，并分页返回。
     */
    @Override
    public BillPageResponse listBills(Long userId, BillPageQueryRequest request) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        // month 例如 2026-10，会转换成 [2026-10-01, 2026-11-01)。
        if (request.getMonth() != null) {
            YearMonth month = YearMonth.parse(request.getMonth());
            startDate = month.atDay(1);
            endDate = month.plusMonths(1).atDay(1);
        }

        // PageHelper 会拦截紧接着的 Mapper 查询，自动分页并统计总数。
        PageHelper.startPage(request.getPage(), request.getPageSize());
        List<BillQueryResult> queryResults = billMapper.selectPageByCondition(
                userId,
                startDate,
                endDate,
                request.getTransactionType(),
                request.getCategoryId()
        );

        PageInfo<BillQueryResult> pageInfo = new PageInfo<>(queryResults);

        // 把数据库查询对象转换成接口文档要求的账单响应对象。
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        List<BillCreateResponse> rows = queryResults.stream()
                .map(result -> new BillCreateResponse(
                        result.getId(),
                        result.getTransactionType(),
                        result.getCategoryId(),
                        result.getCategoryName(),
                        result.getAmount().toPlainString(),
                        result.getTransactionDate(),
                        result.getRemark(),
                        result.getMood(),
                        result.getCreatedAt().atZone(zoneId).toOffsetDateTime(),
                        result.getUpdatedAt().atZone(zoneId).toOffsetDateTime()
                ))
                .collect(Collectors.toList());

        log.debug(
                "账单分页查询完成，userId={}, page={}, pageSize={}, total={}",
                userId,
                request.getPage(),
                request.getPageSize(),
                pageInfo.getTotal()
        );

        return new BillPageResponse(
                pageInfo.getTotal(),
                request.getPage(),
                request.getPageSize(),
                rows
        );
    }
    /**
     * 查询当前用户的账单详情，并转换为接口响应。
     */
    @Override
    public BillCreateResponse getBillDetail(Long userId, Long billId) {
        BillQueryResult result =
                billMapper.selectDetailByIdAndUserId(billId, userId);

        // 不区分“账单不存在”和“账单属于别人”，避免泄露其他用户的数据。
        if (result == null) {
            throw new BillNotFoundException();
        }

        ZoneId zoneId = ZoneId.of("Asia/Shanghai");

        return new BillCreateResponse(
                result.getId(),
                result.getTransactionType(),
                result.getCategoryId(),
                result.getCategoryName(),
                result.getAmount().toPlainString(),
                result.getTransactionDate(),
                result.getRemark(),
                result.getMood(),
                result.getCreatedAt().atZone(zoneId).toOffsetDateTime(),
                result.getUpdatedAt().atZone(zoneId).toOffsetDateTime()
        );
    }
    /**
     * 校验账单归属和新分类后，完整更新账单并返回最新详情。
     */
    @Override
    @Transactional
    public BillCreateResponse updateBill(
            Long userId,
            Long billId,
            BillCreateRequest request) {

        // 先按账单 ID 和当前用户 ID 检查归属；不区分不存在和属于他人。
        BillQueryResult existingBill =
                billMapper.selectDetailByIdAndUserId(billId, userId);
        if (existingBill == null) {
            throw new BillNotFoundException();
        }

        // 修改后的分类也必须存在、启用，并且收支类型匹配。
        Category category = categoryMapper.selectActiveById(request.getCategoryId());
        if (category == null) {
            throw new InvalidParameterException("分类不存在或已停用");
        }
        if (!request.getTransactionType().equals(category.getType())) {
            throw new InvalidParameterException("账单收支类型与分类类型不匹配");
        }

        // 将完整替换后的字段放入实体；用户归属仍由后端身份决定。
        Bill bill = new Bill();
        bill.setId(billId);
        bill.setUserId(userId);
        bill.setCategoryId(request.getCategoryId());
        bill.setTransactionType(request.getTransactionType());
        bill.setAmount(new BigDecimal(request.getAmount()));
        bill.setTransactionDate(request.getTransactionDate());
        bill.setRemark(request.getRemark());
        bill.setMood(request.getMood());

        // 不根据 UPDATE 返回 0 判断账单不存在：字段值未变化时数据库也可能返回 0。
        billMapper.updateByIdAndUserId(bill);

        // 重新查询以取得最新字段和分类名称，作为接口响应。
        BillQueryResult updatedBill =
                billMapper.selectDetailByIdAndUserId(billId, userId);
        if (updatedBill == null) {
            throw new BillNotFoundException();
        }

        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        log.info(
                "账单修改成功，userId={}, billId={}, transactionType={}",
                userId,
                billId,
                updatedBill.getTransactionType()
        );

        return new BillCreateResponse(
                updatedBill.getId(),
                updatedBill.getTransactionType(),
                updatedBill.getCategoryId(),
                updatedBill.getCategoryName(),
                updatedBill.getAmount().toPlainString(),
                updatedBill.getTransactionDate(),
                updatedBill.getRemark(),
                updatedBill.getMood(),
                updatedBill.getCreatedAt().atZone(zoneId).toOffsetDateTime(),
                updatedBill.getUpdatedAt().atZone(zoneId).toOffsetDateTime()
        );
    }
    /**
     * 按账单 ID 和当前用户 ID 物理删除账单。
     */
    @Override
    public void deleteBill(Long userId, Long billId) {
        int affectedRows = billMapper.deleteByIdAndUserId(billId, userId);

        // 0 表示账单不存在或不属于当前用户，对外统一按未找到处理。
        if (affectedRows != 1) {
            throw new BillNotFoundException();
        }

        log.info("账单删除成功，userId={}, billId={}", userId, billId);
    }
}