package com.gec.shop.order.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 订单实体 — 属于 shop-order-api
 *
 * Day08 升级：新增订单号、收货地址、支付时间、多状态流转
 * 状态流：PENDING → PAID → SHIPPING → COMPLETED（正常） / CANCELLED（取消）
 */
@Data
@TableName("t_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;          // Day08 订单号（yyyyMMddHHmmss + 4位随机数）
    private Long pid;                // 商品ID
    private String productName;      // 商品名称（冗余）
    private String productCategory;  // Day09 商品类目（冗余，商家按类目过滤）
    private Double productPrice;     // 商品单价
    private Long uid;                // 用户ID
    private String username;         // 用户名
    private Integer number;          // 数量
    private Double totalAmount;      // 总金额
    private String status;           // PENDING → PAID → SHIPPING → COMPLETED / CANCELLED
    private String receiverName;     // Day08 收货人
    private String receiverPhone;    // Day08 收货人手机
    private String address;          // Day08 收货地址
    private String handledByPort;    // 处理端口（Day02 验证负载均衡用）
    private LocalDateTime createTime;
    private LocalDateTime payTime;   // Day08 支付时间
}
