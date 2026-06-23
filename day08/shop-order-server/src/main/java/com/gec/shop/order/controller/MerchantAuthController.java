package com.gec.shop.order.controller;

import com.gec.shop.common.ResultData;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Day09 商家端简易鉴权（复用 /a/login 的 JWT）
 */
@RestController
@RequestMapping("/m")
public class MerchantAuthController {

    /** 获取当前商家信息 */
    @GetMapping("/current")
    public ResultData<Map<String, Object>> current(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-Role") String role,
            @RequestHeader(value = "X-Category", defaultValue = "") String category) {
        return ResultData.success(Map.of(
                "userId", userId,
                "username", username,
                "role", role,
                "category", category
        ));
    }
}
