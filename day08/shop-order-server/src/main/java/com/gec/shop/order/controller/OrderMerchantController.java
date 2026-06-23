package com.gec.shop.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.shop.common.ResultData;
import com.gec.shop.order.api.entity.Order;
import com.gec.shop.order.mapper.UserCategoryMapper;
import com.gec.shop.order.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Day09 商家端订单管理 — 借鉴 mall4cloud multishop OrderController
 *
 * 核心规则：商家只能看到自己经营类目（category）的订单
 * 从 DB 查询 category（避免 HTTP header 传中文乱码）
 */
@RestController
@RequestMapping("/m/orders")
public class OrderMerchantController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private UserCategoryMapper userCategoryMapper;

    @GetMapping("/page")
    public ResultData<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestHeader("X-User-Id") Long userId) {
        String category = userCategoryMapper.getCategory(userId);
        var wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getProductCategory, category)
                .eq(status != null && !status.isEmpty() && !"全部".equals(status),
                        Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        var page = orderService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size), wrapper);
        return ResultData.success(Map.of(
                "records", page.getRecords(),
                "total", page.getTotal(),
                "current", page.getCurrent(),
                "size", page.getSize(),
                "category", category
        ));
    }

    @GetMapping("/{id}")
    public ResultData<Order> detail(@PathVariable Long id) {
        return ResultData.success(orderService.getById(id));
    }

    @PostMapping("/{id}/delivery")
    public ResultData<Order> delivery(@PathVariable Long id) {
        Order order = orderService.shipOrder(id);
        return order != null ? ResultData.success(order)
                : ResultData.fail(400, "订单不存在或状态不可发货（需PAID）");
    }
}
