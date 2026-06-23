package com.gec.shop.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/** Day09: 查用户类目（避免HTTP header传中文） */
@Mapper
public interface UserCategoryMapper {
    @Select("SELECT category FROM t_user WHERE id = #{userId}")
    String getCategory(Long userId);
}
