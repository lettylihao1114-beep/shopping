package com.gec.shop.order.feign;

import com.gec.shop.product.api.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务 Feign 接口（方案5）
 *
 * @FeignClient(name = "product-service") → 自动从 Nacos 发现 product-service 的实例
 * 底层集成了 Ribbon，默认轮询负载均衡
 */
@FeignClient(name = "product-service", fallback = ProductFeignFallBack.class)
public interface IProductFeignService {

    @GetMapping("/products/{pid}")
    Product get(@PathVariable("pid") Long pid);

    /** Day08 库存扣减/恢复 — 借鉴 mall4cloud SkuStockLockFeignClient.lock() */
    @PutMapping("/products/{pid}/stock")
    String updateStock(@PathVariable("pid") Long pid, @RequestParam("delta") Integer delta);
}
