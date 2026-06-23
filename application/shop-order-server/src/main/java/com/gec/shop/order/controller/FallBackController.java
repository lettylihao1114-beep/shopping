package com.gec.shop.order.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Day03 6.9：Sentinel 降级规则演示
 *
 * 三种降级策略：
 * - 慢调用比例（fallBack1）
 * - 异常比例（fallBack2）
 * - 异常数（fallBack3）
 */
@RestController
@Slf4j
public class FallBackController {

    // ==================== 6.9.1 慢调用比例 ====================

    /** 模拟业务耗时 1s — 当慢调用比例 > 阈值时触发熔断 */
    @RequestMapping("/fallBack1")
    public String fallBack1() {
        try {
            log.info("fallBack1执行业务逻辑");
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "fallBack1";
    }

    // ==================== 6.9.2 异常比例 ====================

    int i = 0;

    /**
     * 模拟异常比例 33%（每 3 次请求抛出 1 次异常）
     * 当异常比例 > 阈值时触发熔断
     */
    @RequestMapping("/fallBack2")
    public String fallBack2() {
        log.info("fallBack2执行业务逻辑");
        if (++i % 3 == 0) {
            throw new RuntimeException();
        }
        return "fallBack2";
    }

    // ==================== 6.9.3 异常数 ====================

    /**
     * 当 name="dafei" 时抛出异常
     * 当异常数 > 阈值时触发熔断
     */
    @RequestMapping("/fallBack3")
    public String fallBack3(String name) {
        log.info("fallBack3执行业务逻辑");
        if ("dafei".equals(name)) {
            throw new RuntimeException();
        }
        return "fallBack3";
    }
}
