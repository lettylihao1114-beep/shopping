package com.gec.shop.payment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

/**
 * Day08 支付服务 → 订单服务 Feign
 * 借鉴 mall4cloud OrderFeignClient
 */
@FeignClient(name = "order-service")
public interface IOrderFeignService {

    /** 标记订单为已支付 */
    @PostMapping("/orders/{id}/pay")
    Map<String, Object> payOrder(@PathVariable("id") Long id);
}
