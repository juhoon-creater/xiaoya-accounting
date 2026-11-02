package com.xiaoya.accounting.controller;

import com.xiaoya.accounting.common.ApiResponse;
import com.xiaoya.accounting.dto.BillCreateRequest;
import com.xiaoya.accounting.dto.BillCreateResponse;
import com.xiaoya.accounting.dto.BillPageQueryRequest;
import com.xiaoya.accounting.dto.BillPageResponse;
import com.xiaoya.accounting.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 账单接口控制器，负责接收账单相关的 HTTP 请求。
 */
@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
public class BillController {

    /** 账单业务对象，由构造器注入。 */
    private final BillService billService;

    /**
     * 为当前登录用户新增一笔账单。
     *
     * @param userId Gateway 验证 Token 后写入的当前用户 ID
     * @param request App 提交的账单数据
     * @return HTTP 201 和新增账单详情
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BillCreateResponse>> createBill(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BillCreateRequest request) {

        BillCreateResponse response = billService.createBill(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
    /**
     * 分页查询当前登录用户的账单。
     *
     * @param userId Gateway 验证 Token 后写入的当前用户 ID
     * @param request URL 中的筛选和分页参数
     * @return HTTP 200 和分页账单数据
     */
    @GetMapping
    public ApiResponse<BillPageResponse> listBills(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @ModelAttribute BillPageQueryRequest request) {

        BillPageResponse response = billService.listBills(userId, request);
        return ApiResponse.success(response);
    }
    /**
     * 查询当前登录用户的一笔账单详情。
     *
     * @param userId Gateway 验证 Token 后写入的当前用户 ID
     * @param billId 路径中的账单 ID
     * @return HTTP 200 和账单详情
     */
    @GetMapping("/{billId}")
    public ApiResponse<BillCreateResponse> getBillDetail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long billId) {

        BillCreateResponse response = billService.getBillDetail(userId, billId);
        return ApiResponse.success(response);
    }
    /**
     * 完整修改当前登录用户的一笔账单。
     *
     * @param userId Gateway 验证 Token 后写入的当前用户 ID
     * @param billId 路径中的账单 ID
     * @param request 修改后的完整账单数据
     * @return HTTP 200 和修改后的账单详情
     */
    @PutMapping("/{billId}")
    public ApiResponse<BillCreateResponse> updateBill(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("billId") Long billId,
            @Valid @RequestBody BillCreateRequest request) {

        BillCreateResponse response =
                billService.updateBill(userId, billId, request);

        return ApiResponse.success(response);
    }
    /**
     * 删除当前登录用户的一笔账单。
     *
     * @param userId Gateway 验证 Token 后写入的当前用户 ID
     * @param billId 路径中的账单 ID
     * @return HTTP 200，成功响应中的 data 为 null
     */
    @DeleteMapping("/{billId}")
    public ApiResponse<Void> deleteBill(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("billId") Long billId) {

        billService.deleteBill(userId, billId);
        return ApiResponse.success(null);
    }
}