package com.gec.shop.product.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品实体 — 属于 shop-product-api
 * 其他服务想用 Product 类，只需依赖这个薄 jar
 */
@Data
@TableName("t_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long pid;
    private String name;
    private Double price;
    private String image;
    private String category;
    private String description;
    private Integer stock;
    private Double rating;
    private Integer reviewCount;
}
