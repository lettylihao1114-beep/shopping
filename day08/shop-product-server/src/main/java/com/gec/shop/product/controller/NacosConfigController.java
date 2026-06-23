package com.gec.shop.product.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day05 Nacos Config 动态刷新演示
 *
 * @RefreshScope 的作用：
 * 当 Nacos 上的配置发生变更时，标注了 @RefreshScope 的 Bean 会被重新创建，
 * 从而获取到最新的配置值，无需重启服务。
 */
@RestController
@RequestMapping("/config")
@RefreshScope
public class NacosConfigController {

    /** 来自 product-service-dev.yaml 的环境配置 */
    @Value("${appConfig.name:未配置}")
    private String appConfigName;

    /** 来自 global-config.yaml 的全局共享配置 */
    @Value("${globalConfig:未配置}")
    private String globalConfig;

    /**
     * 环境独有配置
     * GET http://localhost:8081/config/name
     */
    @GetMapping("/name")
    public String getAppConfigName() {
        return "appConfig.name(环境独有) = 【" + appConfigName + "】";
    }

    /**
     * 全局共享配置
     * GET http://localhost:8081/config/global
     */
    @GetMapping("/global")
    public String getGlobalConfig() {
        return "globalConfig(全局共享) = 【" + globalConfig + "】";
    }
}
