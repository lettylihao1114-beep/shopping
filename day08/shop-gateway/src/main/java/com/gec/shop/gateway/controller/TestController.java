package com.gec.shop.gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day04 API 分组测试控制器
 *
 * 对应课件 7.9.2 节 — Sentinel API 分组限流演示
 * 三个 endpoint 用于演示精准匹配 / 前缀匹配 / 正则匹配
 *
 * 通过网关访问：http://localhost:9000/v1/test1?token=123
 */
@RestController
@RequestMapping("/v1")
public class TestController {

    @RequestMapping("/test1")
    public String test1() {
        return "test1";
    }

    @RequestMapping("/test2")
    public String test2() {
        return "test2";
    }

    @RequestMapping("/test3/test")
    public String test3() {
        return "test3";
    }
}
