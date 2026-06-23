package com.gec.shop.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.gec.shop.auth")
@MapperScan("com.gec.shop.auth.mapper")
@EnableDiscoveryClient
public class AuthServer {
    public static void main(String[] args) { SpringApplication.run(AuthServer.class, args); }
}
