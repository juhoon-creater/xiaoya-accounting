package com.xiaoya.accounting.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一包装记账服务的接口成功响应。
 *
 * @param <T> 响应数据的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 业务响应码，1 表示成功。
     */
    private Integer code;

    /**
     * 响应说明。
     */
    private String msg;

    /**
     * 接口实际返回的数据。
     */
    private T data;

    /**
     * 创建成功响应。
     *
     * @param data 接口返回的数据
     * @param <T> 数据类型
     * @return 包装后的成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(1, "success", data);
    }
}