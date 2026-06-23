package com.gec.shop.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 支付记录 — 借鉴 mall4cloud PayInfo
 * 简化版：去 LeafID / version / sysType / callbackContent
 */
@Data
@TableName("t_pay_info")
public class PayInfo {
    @TableId(type = IdType.AUTO)
    private Long payId;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Double amount;
    private String status;       // UNPAID / PAID
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}
