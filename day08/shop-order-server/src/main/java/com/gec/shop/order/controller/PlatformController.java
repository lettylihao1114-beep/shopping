package com.gec.shop.order.controller;

import com.gec.shop.common.ResultData;
import com.gec.shop.order.api.entity.Order;
import com.gec.shop.order.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Day09 平台端 — 数据概览 + 全部订单 + 用户列表
 */
@RestController
@RequestMapping("/platform")
public class PlatformController {

    @Autowired
    private IOrderService orderService;

    /** 数据概览 */
    @GetMapping("/dashboard")
    public ResultData<Map<String, Object>> dashboard() {
        long totalOrders = orderService.count();
        long pendingOrders = orderService.lambdaQuery().eq(Order::getStatus, "PENDING").count();
        long paidOrders = orderService.lambdaQuery().eq(Order::getStatus, "PAID").count();
        long shippingOrders = orderService.lambdaQuery().eq(Order::getStatus, "SHIPPING").count();
        long completedOrders = orderService.lambdaQuery().eq(Order::getStatus, "COMPLETED").count();
        long cancelledOrders = orderService.lambdaQuery().eq(Order::getStatus, "CANCELLED").count();

        // 今日订单
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayOrders = orderService.lambdaQuery().ge(Order::getCreateTime, todayStart).count();

        return ResultData.success(Map.of(
                "totalOrders", totalOrders,
                "todayOrders", todayOrders,
                "pendingOrders", pendingOrders,
                "paidOrders", paidOrders,
                "shippingOrders", shippingOrders,
                "completedOrders", completedOrders,
                "cancelledOrders", cancelledOrders
        ));
    }

    /** 全部订单 */
    @GetMapping("/orders")
    public ResultData<List<Order>> orders(@RequestParam(required = false) String status) {
        List<Order> list = orderService.lambdaQuery()
                .eq(status != null && !status.isEmpty(), Order::getStatus, status)
                .orderByDesc(Order::getCreateTime)
                .list();
        return ResultData.success(list);
    }
}
