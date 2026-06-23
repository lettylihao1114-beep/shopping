package com.gec.shop.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Day02：@LoadBalanced 让 RestTemplate 具备 Ribbon 负载均衡能力
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced   // ← 方案4 关键：用服务名替代 IP:Port
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
