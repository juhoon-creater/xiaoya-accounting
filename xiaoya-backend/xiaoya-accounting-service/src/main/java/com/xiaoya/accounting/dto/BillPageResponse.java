package com.xiaoya.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询账单接口返回的分页数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillPageResponse {

    /** 符合筛选条件的账单总数。 */
    private Long total;

    /** 当前页码，从 1 开始。 */
    private Integer page;

    /** 每页返回的账单条数。 */
    private Integer pageSize;

    /** 当前页账单列表，字段结构与账单详情相同。 */
    private List<BillCreateResponse> rows;
}