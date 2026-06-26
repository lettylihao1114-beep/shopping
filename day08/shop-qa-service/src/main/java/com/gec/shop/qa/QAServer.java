package com.gec.shop.qa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.gec.shop.qa")
@EnableDiscoveryClient
public class QAServer {
    public static void main(String[] args) {
        SpringApplication.run(QAServer.class, args);
    }
}
