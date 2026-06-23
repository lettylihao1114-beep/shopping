package com.gec.shop.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Day08 支付微服务启动类 */
@SpringBootApplication(scanBasePackages = "com.gec.shop.payment")
@MapperScan("com.gec.shop.payment.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class PaymentServer {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServer.class, args);
    }
}
