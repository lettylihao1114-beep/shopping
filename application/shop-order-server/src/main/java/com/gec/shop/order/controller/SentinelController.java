package com.gec.shop.order.controller;

import com.gec.shop.order.service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Day03：Sentinel 流控演示控制器
 */
@RestController
public class SentinelController {

    @Autowired
    private GoodService goodService;

    // ==================== 6.1 高并发模拟 ====================

    /** 模拟网络延时 1 秒 — 压测时大量请求在此堆积，导致 sentinel2 不可用 */
    @RequestMapping("/sentinel1")
    public String sentinel1() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "sentinel1";
    }

    /** 普通接口 — 当 sentinel1 耗尽所有线程时，此接口也会被影响（服务雪崩雏形） */
    @RequestMapping("/sentinel2")
    public String sentinel2() {
        return "测试高并发下的问题";
    }

    // ==================== 6.8.2.2 关联流控模式 ====================

    /** 读请求 — 关联 /sentinel-write，当写请求 QPS 超阈值时会对读请求限流 */
    @RequestMapping("/sentinel-read")
    public String readReq() {
        return "读请求";
    }

    /** 写请求 — 作为关联资源，压测此接口会触发 /sentinel-read 的限流 */
    @RequestMapping("/sentinel-write")
    public String writeReq() {
        return "写请求";
    }

    // ==================== 6.8.2.3 链路流控模式 ====================

    /** 查询订单 — 调用 queryGood()，链路流控针对此入口限流 */
    @RequestMapping("/queryOrder")
    public String queryOrder() {
        goodService.queryGood();
        return "查询订单";
    }

    /** 创建订单 — 同样调用 queryGood()，但链路流控不限此入口 */
    @RequestMapping("/createOrder")
    public String createOrder() {
        goodService.queryGood();
        return "创建订单订单";
    }
}
