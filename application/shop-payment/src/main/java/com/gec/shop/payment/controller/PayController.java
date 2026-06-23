package com.gec.shop.payment.controller;

import com.gec.shop.common.ResultData;
import com.gec.shop.payment.entity.PayInfo;
import com.gec.shop.payment.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付控制器 — 借鉴 mall4cloud PayController
 *
 * POST /payment/pay  → 创建支付记录 → 调订单服务标记PAID → 返回支付结果
 * GET  /payment/{id}  → 查询支付记录
 */
@RestController
@RequestMapping("/payment")
public class PayController {

    @Autowired
    private PayService payService;

    @PostMapping("/pay")
    public ResultData<PayInfo> pay(@RequestBody Map<String, Object> body,
                                   @RequestHeader("X-User-Id") Long userId) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        PayInfo payInfo = payService.pay(orderId, userId);
        if ("PAID".equals(payInfo.getStatus())) {
            return ResultData.success(payInfo);
        }
        return ResultData.fail(400, "支付失败");
    }

    @GetMapping("/{payId}")
    public ResultData<PayInfo> query(@PathVariable Long payId) {
        PayInfo payInfo = payService.getById(payId);
        return payInfo != null ? ResultData.success(payInfo)
                : ResultData.fail(404, "支付记录不存在");
    }
}
