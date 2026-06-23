package com.gec.shop.auth.controller;

import com.gec.shop.auth.service.impl.AuthServiceImpl;
import com.gec.shop.common.LoginUser;
import com.gec.shop.common.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/a")
public class AuthController {
    @Autowired private AuthServiceImpl authService;

    @PostMapping("/login")
    public ResultData<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String token = authService.login(body.get("username"), body.get("password"));
        return token != null ? ResultData.success(Map.of("token", token))
                : ResultData.fail(401, "用户名或密码错误");
    }

    @PostMapping("/register")
    public ResultData<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String token = authService.register(body.get("username"), body.get("password"), body.get("phone"));
        return token != null ? ResultData.success(Map.of("token", token))
                : ResultData.fail(400, "用户名已存在");
    }

    @GetMapping("/current")
    public ResultData<LoginUser> current(@RequestHeader("X-User-Id") Long userId) {
        LoginUser u = authService.current(userId);
        return u != null ? ResultData.success(u) : ResultData.fail(404, "用户不存在");
    }
}
