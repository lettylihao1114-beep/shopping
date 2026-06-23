package com.gec.shop.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class GatewayConfiguration {
    @PostConstruct
    public void initBlockHandlers() {
        GatewayCallbackManager.setBlockHandler((exchange, throwable) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 429);
            map.put("message", "接口被限流了，请稍后再试");
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(map));
        });
    }
}
