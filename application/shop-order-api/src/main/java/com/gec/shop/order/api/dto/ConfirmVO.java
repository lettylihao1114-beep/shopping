package com.gec.shop.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Day08 订单确认返回 — 借鉴 mall4cloud ShopCartOrderMergerVO
 * 包含实时价格计算 + 防重 token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmVO {
    /** 订单项（含实时价格） */
    private List<ConfirmItem> items;
    /** 商品总金额 */
    private Double totalAmount;
    /** 运费 */
    private Double deliveryFee;
    /** 应付金额 */
    private Double payAmount;
    /** 防重令牌（submit 时必须回传） */
    private String confirmToken;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmItem {
        private Long pid;
        private String name;
        private Double price;
        private Integer qty;
        private Double subtotal;
        private Integer stock;       // 当前库存
        private Boolean inStock;     // 库存是否充足
    }
}
