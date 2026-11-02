package com.xiaoya.accounting.mapper;

import com.xiaoya.accounting.dto.BillQueryResult;
import com.xiaoya.accounting.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单数据访问接口，对应 SQL 写在 resources/mapper/BillMapper.xml 中。
 */
@Mapper
public interface BillMapper {

    /**
     * 新增一条账单，插入后会把数据库生成的主键写入 bill.id。
     *
     * @param bill 待新增的账单
     * @return 受影响的行数
     */
    int insert(Bill bill);

    /**
     * 根据账单 ID 和用户 ID 查询账单。
     *
     * 同时匹配 userId 可以避免通过账单 ID 读取到其他用户的账单。
     *
     * @param id 账单 ID
     * @param userId 当前用户 ID
     * @return 找到时返回账单；找不到时返回 null
     */
    Bill selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    /**
     * 根据用户和筛选条件查询账单。
     * 分页及总数统计由 PageHelper 拦截这次查询完成。
     */
    List<BillQueryResult> selectPageByCondition(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("transactionType") String transactionType,
            @Param("categoryId") Long categoryId
    );
    /**
     * 查询当前用户的一笔账单，并关联取得分类名称。
     *
     * @param id 账单 ID
     * @param userId 当前登录用户 ID
     * @return 查到时返回账单详情；账单不存在或不属于当前用户时返回 null
     */
    BillQueryResult selectDetailByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );
    /**
     * 修改当前用户的一笔账单。
     *
     * @param bill 携带账单 ID、当前用户 ID 和待更新字段
     * @return 数据库报告的受影响行数
     */
    int updateByIdAndUserId(@Param("bill") Bill bill);
    /**
     * 物理删除当前用户的一笔账单。
     *
     * @param id 账单 ID
     * @param userId 当前登录用户 ID
     * @return 删除的记录数；未找到或不属于该用户时为 0
     */
    int deleteByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );
}