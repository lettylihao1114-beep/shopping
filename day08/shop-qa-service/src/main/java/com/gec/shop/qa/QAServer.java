package com.gec.shop.qa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.gec.shop.qa.service")
public class QAServer {
    public static void main(String[] args) {
        SpringApplication.run(QAServer.class, args);
    }
}
