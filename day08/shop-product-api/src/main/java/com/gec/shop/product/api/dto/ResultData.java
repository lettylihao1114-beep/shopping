package com.gec.shop.product.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体 — 放在 product-api 中，所有服务共用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultData<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ResultData<T> success(T data) {
        return new ResultData<>(0, "success", data);
    }

    public static <T> ResultData<T> fail(int code, String message) {
        return new ResultData<>(code, message, null);
    }
}
