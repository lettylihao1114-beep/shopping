package com.gec.shop.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 订单微服务启动类
 * Day02：加 @EnableFeignClients 开启 Feign 远程调用
 */
@SpringBootApplication(scanBasePackages = "com.gec.shop.order")
@MapperScan("com.gec.shop.order.mapper")
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling             // ← Day08 新增：延时关单定时任务
public class OrderServer {
    public static void main(String[] args) {
        SpringApplication.run(OrderServer.class, args);
    }
}
