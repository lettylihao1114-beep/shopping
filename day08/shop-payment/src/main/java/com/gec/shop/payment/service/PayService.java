package com.gec.shop.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.shop.payment.entity.PayInfo;

public interface PayService extends IService<PayInfo> {
    /** 支付 — 借鉴 mall4cloud PayInfoServiceImpl.pay() */
    PayInfo pay(Long orderId, Long userId);
}
