package com.gec.shop.order.api.dto;

import lombok.Data;
import java.util.List;

/**
 * Day08 订单提交请求
 * 前端 ConfirmOrder 页 → Gateway → order-service
 */
@Data
public class OrderSubmitRequest {
    /** Day08 防重令牌（从 /orders/confirm 获取，必填） */
    private String confirmToken;
    /** 订单项（可多个商品） */
    private List<OrderItem> items;
    /** 收货人 */
    private String receiverName;
    /** 收货人手机 */
    private String receiverPhone;
    /** 收货地址 */
    private String address;

    @Data
    public static class OrderItem {
        private Long pid;
        private Integer qty;
    }
}
