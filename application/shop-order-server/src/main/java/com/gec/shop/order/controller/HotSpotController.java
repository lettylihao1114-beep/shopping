package com.gec.shop.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day03 6.10：Sentinel 热点参数限流
 *
 * 热点即经常访问的数据。统计热点数据中访问频次最高的 Top K，对其访问进行限制。
 * 注意：必须在方法上贴 @SentinelResource，否则热点规则无效！
 */
@RestController
@Slf4j
public class HotSpotController {

    @RequestMapping("/hotSpot1")
    @SentinelResource(value = "hotSpot1")
    public String hotSpot1(Long productId) {
        log.info("访问编号为:{}的商品", productId);
        return "hotSpot1";
    }
}
