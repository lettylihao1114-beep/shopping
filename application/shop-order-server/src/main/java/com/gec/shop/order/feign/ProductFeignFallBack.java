package com.gec.shop.order.feign;

import com.gec.shop.product.api.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Day03 6.15：Feign 整合 Sentinel 容错类
 *
 * 当商品服务不可用时（超时、异常、宕机），返回兜底数据
 * 保证订单服务不会因为商品服务挂掉而崩溃
 */
@Component
public class ProductFeignFallBack implements IProductFeignService {

    @Override
    public Product get(Long pid) {
        Product product = new Product();
        product.setPid(-1L);
        product.setName("兜底数据");
        product.setPrice(0.0);
        return product;
    }

    @Override
    public String updateStock(Long pid, Integer delta) {
        return "fallback";
    }
}
