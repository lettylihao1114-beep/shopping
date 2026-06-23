package com.gec.shop.order.controller;

import com.gec.shop.common.ConfirmCache;
import com.gec.shop.common.ResultData;
import com.gec.shop.order.api.dto.ConfirmRequest;
import com.gec.shop.order.api.dto.ConfirmVO;
import com.gec.shop.order.feign.IProductFeignService;
import com.gec.shop.product.api.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Day08 订单确认控制器 — 借鉴 mall4cloud OrderController.confirm()
 *
 * 两步式下单：
 *   1. POST /orders/confirm → 实时查价 + 验库存 + 生成防重token
 *   2. POST /orders/submit  → 带 token 提交（token 只能用一次）
 */
@RestController
@RequestMapping("/orders")
public class ConfirmController {

    @Autowired
    private IProductFeignService productFeignService;

    @PostMapping("/confirm")
    public ResultData<ConfirmVO> confirm(@RequestBody ConfirmRequest req) {
        List<ConfirmVO.ConfirmItem> items = new ArrayList<>();
        double total = 0;

        for (var item : req.getItems()) {
            Product p = productFeignService.get(item.getPid());
            if (p.getPid() == null || p.getPid() <= 0) {
                return ResultData.fail(400, "商品 #" + item.getPid() + " 不存在");
            }
            double subtotal = p.getPrice() * item.getQty();
            boolean inStock = p.getStock() >= item.getQty();

            items.add(new ConfirmVO.ConfirmItem(
                    p.getPid(), p.getName(), p.getPrice(),
                    item.getQty(), subtotal, p.getStock(), inStock));
            total += subtotal;
        }

        // 构建确认单，存入缓存，返回 token
        ConfirmVO vo = new ConfirmVO(items, total, 0.0, total, ConfirmCache.put(req));
        return ResultData.success(vo);
    }
}
