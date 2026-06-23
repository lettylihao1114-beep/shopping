package com.gec.shop.order.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day03 6.11：Sentinel 授权规则（黑白名单）
 *
 * 根据请求来源（origin）判断是否放行
 */
@RestController
@Slf4j
public class AuthController {

    @RequestMapping("/auth1")
    public String auth1(String serviceName) {
        log.info("应用:{},访问接口", serviceName);
        return "auth1";
    }
}
