package com.gec.shop.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Day04 API 网关启动类
 *
 * 网关是整个系统的统一入口：
 * - 路由转发 → 根据 Path 将请求转发到对应的微服务
 * - 负载均衡 → lb://service-name 自动 Ribbon 轮询
 * - 鉴权过滤 → GlobalFilter 检查 token
 * - Sentinel 限流 → 在网关层对 route / API 分组进行流控
 */
@SpringBootApplication(scanBasePackages = "com.gec.shop.gateway")
@EnableDiscoveryClient
public class ApiGatewayServer {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayServer.class, args);
    }
}
