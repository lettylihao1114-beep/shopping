package com.gec.shop.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.shop.auth.entity.User;
import com.gec.shop.auth.mapper.UserMapper;
import com.gec.shop.common.JwtUtil;
import com.gec.shop.common.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl {
    @Autowired private UserMapper userMapper;

    public String login(String username, String password) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (u == null || !u.getPassword().equals(password)) return null;
        return JwtUtil.create(u.getId(), u.getUsername(), u.getRole(), u.getCategory());
    }

    public String register(String username, String password, String phone) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0)
            return null;
        User u = new User(); u.setUsername(username); u.setPassword(password); u.setPhone(phone); u.setRole("user");
        userMapper.insert(u);
        return JwtUtil.create(u.getId(), u.getUsername(), u.getRole(), u.getCategory());
    }

    public LoginUser current(Long userId) {
        User u = userMapper.selectById(userId);
        return u != null ? new LoginUser(u.getId(), u.getUsername(), u.getRole(), u.getCategory()) : null;
    }
}
