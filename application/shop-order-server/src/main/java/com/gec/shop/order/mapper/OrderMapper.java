package com.gec.shop.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.shop.order.api.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
