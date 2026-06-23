package com.gec.shop.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 商品微服务启动类
 * @EnableDiscoveryClient → 注册到 Nacos
 */
@SpringBootApplication(scanBasePackages = "com.gec.shop.product")
@MapperScan("com.gec.shop.product.mapper")
@EnableDiscoveryClient
public class ProductServer {
    public static void main(String[] args) {
        SpringApplication.run(ProductServer.class, args);
    }
}
