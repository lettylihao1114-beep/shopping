package com.gec.shop.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String username;
    private String role;     // platform / shop / user
    private String category;  // Day09: 商家经营类目（shop角色使用）

    public LoginUser(Long userId, String username, String role) {
        this(userId, username, role, null);
    }
}
