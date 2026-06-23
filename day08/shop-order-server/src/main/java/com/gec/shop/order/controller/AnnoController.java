package com.gec.shop.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day03 6.14：@SentinelResource 注解使用
 *
 * 演示 blockHandler（流控/降级触发时进入）和 fallback（业务异常时进入）两种兜底策略
 */
@RestController
@Slf4j
public class AnnoController {

    @RequestMapping("/anno1")
    @SentinelResource(
            value = "anno1",
            blockHandler = "anno1BlockHandler",   // 被 Sentinel 限流/降级时进入
            fallback = "anno1Fallback"            // 业务方法抛异常时进入
    )
    public String anno1(String name) {
        if ("dafei".equals(name)) {
            throw new RuntimeException();
        }
        return "anno1";
    }

    /** 限流/降级处理方法 — 参数必须包含原方法参数 + BlockException */
    public String anno1BlockHandler(String name, BlockException ex) {
        log.error("{}", ex);
        return "接口被限流或者降级了";
    }

    /** 业务异常兜底方法 — 参数必须包含原方法参数 + Throwable */
    public String anno1Fallback(String name, Throwable throwable) {
        log.error("{}", throwable);
        return "接口发生异常了";
    }
}
