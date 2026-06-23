package com.gec.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.shop.product.api.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
