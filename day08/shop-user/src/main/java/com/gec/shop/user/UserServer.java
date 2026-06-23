package com.gec.shop.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.gec.shop.user")
@MapperScan("com.gec.shop.user.mapper")
@EnableDiscoveryClient
public class UserServer {
    public static void main(String[] args) { SpringApplication.run(UserServer.class, args); }
}
