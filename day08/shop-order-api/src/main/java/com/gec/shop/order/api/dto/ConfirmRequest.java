package com.gec.shop.order.api.dto;

import lombok.Data;
import java.util.List;

/**
 * Day08 订单确认请求 — 借鉴 mall4cloud OrderDTO
 * 前端 → POST /orders/confirm
 */
@Data
public class ConfirmRequest {
    private List<OrderSubmitRequest.OrderItem> items;
    private String receiverName;
    private String receiverPhone;
    private String address;
}
