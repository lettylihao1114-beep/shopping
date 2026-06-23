package com.gec.shop.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.shop.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
