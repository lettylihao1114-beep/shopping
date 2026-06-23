package com.gec.shop.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.shop.payment.entity.PayInfo;
import com.gec.shop.payment.feign.IOrderFeignService;
import com.gec.shop.payment.mapper.PayInfoMapper;
import com.gec.shop.payment.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付服务实现 — 借鉴 mall4cloud PayInfoServiceImpl
 *
 * 简化：去 Leaf 分布式ID / MQ 通知 / 外部支付网关回调
 */
@Slf4j
@Service
public class PayServiceImpl extends ServiceImpl<PayInfoMapper, PayInfo> implements PayService {

    @Autowired
    private IOrderFeignService orderFeignService;

    @Override
    public PayInfo pay(Long orderId, Long userId) {
        // 1. 创建支付记录（UNPAID）
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderId(orderId);
        payInfo.setUserId(userId);
        payInfo.setStatus("UNPAID");
        payInfo.setCreateTime(LocalDateTime.now());
        super.save(payInfo);
        log.info("支付记录创建: payId={} orderId={}", payInfo.getPayId(), orderId);

        // 2. 通知订单服务标记已支付
        Map<String, Object> result = orderFeignService.payOrder(orderId);
        int code = (int) result.getOrDefault("code", -1);
        if (code != 0) {
            log.warn("订单支付标记失败: orderId={} msg={}", orderId, result.get("message"));
            payInfo.setStatus("FAILED");
            super.updateById(payInfo);
            return payInfo;
        }

        // 3. 标记支付成功
        Map<String, Object> orderData = (Map<String, Object>) result.get("data");
        if (orderData != null) {
            payInfo.setOrderNo((String) orderData.get("orderNo"));
            Object amt = orderData.get("totalAmount");
            if (amt != null) payInfo.setAmount(((Number) amt).doubleValue());
        }
        payInfo.setStatus("PAID");
        payInfo.setPayTime(LocalDateTime.now());
        super.updateById(payInfo);
        log.info("支付成功: payId={} orderId={} amount={}", payInfo.getPayId(), orderId, payInfo.getAmount());
        return payInfo;
    }
}
