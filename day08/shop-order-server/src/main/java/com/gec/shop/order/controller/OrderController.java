package com.gec.shop.order.controller;

import com.gec.shop.common.ConfirmCache;
import com.gec.shop.common.ResultData;
import com.gec.shop.order.api.dto.ConfirmRequest;
import com.gec.shop.order.api.dto.OrderSubmitRequest;
import com.gec.shop.order.api.entity.Order;
import com.gec.shop.order.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器 — Day08 升级：confirm / submit(防重) / pay / cancel / ship / receive
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    // ==================== Day08 核心 ====================

    /**
     * 提交订单 — 借鉴 mall4cloud OrderController.submitOrders()
     * 必须带 confirmToken（从 /orders/confirm 获取，一次性使用）
     */
    @PostMapping("/submit")
    public ResultData<List<Order>> submit(@RequestBody OrderSubmitRequest req,
                                          @RequestHeader("X-User-Id") Long userId,
                                          @RequestHeader("X-Username") String username) {
        // 防重检查：从缓存取出并删除 confirmToken
        ConfirmRequest cached = ConfirmCache.getAndRemove(req.getConfirmToken());
        if (cached == null) {
            return ResultData.fail(400, "订单已过期或重复提交，请重新确认订单");
        }
        List<Order> orders = orderService.submitOrder(req, userId, username);
        return ResultData.success(orders);
    }

    /** 支付（保留兼容 — 推荐用 shop-payment 微服务） */
    @PostMapping("/{id}/pay")
    public ResultData<Order> pay(@PathVariable Long id) {
        Order order = orderService.payOrder(id);
        return order != null ? ResultData.success(order)
                : ResultData.fail(400, "订单不存在或状态不可支付");
    }

    /** 取消订单 — 借鉴 mall4cloud cancelOrderAndGetCancelOrderIds() */
    @PutMapping("/{id}/cancel")
    public ResultData<String> cancel(@PathVariable Long id) {
        boolean ok = orderService.cancelOrder(id);
        return ok ? ResultData.success("订单已取消")
                : ResultData.fail(400, "订单不存在或无法取消");
    }

    /** Day08 发货 — 借鉴 mall4cloud OrderServiceImpl.delivery() */
    @PutMapping("/{id}/ship")
    public ResultData<Order> ship(@PathVariable Long id) {
        Order order = orderService.shipOrder(id);
        return order != null ? ResultData.success(order)
                : ResultData.fail(400, "订单不存在或状态不可发货（需PAID）");
    }

    /** Day08 确认收货 — 借鉴 mall4cloud OrderServiceImpl.receiptOrder() */
    @PutMapping("/{id}/receive")
    public ResultData<Order> receive(@PathVariable Long id) {
        Order order = orderService.receiveOrder(id);
        return order != null ? ResultData.success(order)
                : ResultData.fail(400, "订单不存在或状态不可收货（需SHIPPING）");
    }

    // ==================== 原有接口 ====================

    @GetMapping("/save")
    public ResultData<Order> save(@RequestParam Long pid, @RequestParam Long uid) {
        Order order = orderService.createOrder(pid, uid);
        return ResultData.success(order);
    }

    @GetMapping
    public ResultData<List<Order>> list(@RequestParam(required = false) String status,
                                        @RequestHeader("X-User-Id") Long userId) {
        List<Order> list = orderService.lambdaQuery()
                .eq(Order::getUid, userId)
                .eq(status != null && !status.isEmpty() && !"全部".equals(status),
                        Order::getStatus, status)
                .orderByDesc(Order::getCreateTime)
                .list();
        return ResultData.success(list);
    }

    @GetMapping("/{id}")
    public ResultData<Order> detail(@PathVariable Long id) {
        return ResultData.success(orderService.getById(id));
    }
}
