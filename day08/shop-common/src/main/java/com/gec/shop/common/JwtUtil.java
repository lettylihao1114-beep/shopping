package com.gec.shop.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtUtil {
    // 固定密钥 — 所有服务共用，生产环境应放配置中心
    private static final String SECRET = "yuexuan-mall4cloud-jwt-secret-key-2025-springcloud-alibaba";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRE = 7 * 24 * 3600 * 1000L; // 7 天

    public static String create(Long userId, String username, String role, String category) {
        return Jwts.builder()
                .claim("userId", userId).claim("username", username)
                .claim("role", role).claim("category", category)
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(KEY).compact();
    }

    public static LoginUser parse(String token) {
        try {
            Claims c = Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token).getBody();
            return new LoginUser(
                    c.get("userId", Long.class), c.get("username", String.class),
                    c.get("role", String.class), c.get("category", String.class));
        } catch (Exception e) { return null; }
    }
}
