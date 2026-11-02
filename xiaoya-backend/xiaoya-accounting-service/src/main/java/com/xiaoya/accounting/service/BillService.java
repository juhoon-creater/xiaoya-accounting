package com.xiaoya.accounting.service;

import com.xiaoya.accounting.dto.BillCreateRequest;
import com.xiaoya.accounting.dto.BillCreateResponse;
import com.xiaoya.accounting.dto.BillPageQueryRequest;
import com.xiaoya.accounting.dto.BillPageResponse;

/**
 * 账单业务接口，声明账单相关的业务操作。
 */
public interface BillService {

    /**
     * 为当前用户新增一笔账单。
     *
     * @param userId 当前登录用户 ID，由后端从已验证的身份信息中取得
     * @param request 前端提交的新增账单数据
     * @return 新增成功后的账单详情
     */
    BillCreateResponse createBill(Long userId, BillCreateRequest request);
    /**
     * 分页查询当前用户的账单。
     *
     * @param userId 当前登录用户 ID
     * @param request 筛选与分页参数
     * @return 分页账单数据
     */
    BillPageResponse listBills(Long userId, BillPageQueryRequest request);
    /**
     * 查询当前用户的一笔账单详情。
     *
     * @param userId 当前登录用户 ID
     * @param billId 账单 ID
     * @return 账单详情
     */
    BillCreateResponse getBillDetail(Long userId, Long billId);
    /**
     * 完整修改当前用户的一笔账单。
     *
     * @param userId 当前登录用户 ID
     * @param billId 要修改的账单 ID
     * @param request 修改后的完整账单数据
     * @return 修改后的账单详情
     */
    BillCreateResponse updateBill(
            Long userId,
            Long billId,
            BillCreateRequest request
    );
    /**
     * 删除当前用户的一笔账单。
     *
     * @param userId 当前登录用户 ID
     * @param billId 要删除的账单 ID
     */
    void deleteBill(Long userId, Long billId);
}