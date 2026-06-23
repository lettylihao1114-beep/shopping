package com.gec.shop.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.shop.order.api.dto.OrderSubmitRequest;
import com.gec.shop.order.api.entity.Order;

import java.util.List;

/**
 * Day08 升级：submitOrder(库存扣减) / payOrder / cancelOrder(库存恢复) / shipOrder / receiveOrder
 */
public interface IOrderService extends IService<Order> {
    Order createOrder(Long pid, Long uid);
    List<Order> submitOrder(OrderSubmitRequest req, Long userId, String username);
    Order payOrder(Long id);
    boolean cancelOrder(Long id);

    /** Day08 发货 — 借鉴 mall4cloud delivery() */
    Order shipOrder(Long id);

    /** Day08 确认收货 — 借鉴 mall4cloud receiptOrder() */
    Order receiveOrder(Long id);

    /** Day08 自动取消过期订单 — 借鉴 mall4cloud cancelOrderAndGetCancelOrderIds() */
    int autoCancelExpiredOrders();
}
