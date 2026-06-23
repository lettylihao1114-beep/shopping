package com.gec.shop.gateway.config;

import com.gec.shop.common.JwtUtil;
import com.gec.shop.common.LoginUser;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Day07 全局 JWT 鉴权过滤器
 *
 * 白名单路径（无需登录）：
 *   POST /a/login, POST /a/register
 *
 * 其他路径：从 Authorization Header 提取 Bearer Token，解析 JWT
 * 解析成功后，将 userId/username/role 放入 Header 传给下游
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITELIST = List.of(
        "/a/login", "/a/register",
        "/yolo"     // AI 识物：算法层自身无鉴权，由应用层按需加业务校验
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单放行
        if (WHITELIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // JWT 鉴权
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        LoginUser user = JwtUtil.parse(token);
        if (user == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Day09 角色校验：/m/** → shop/admin/platform, /platform/** → admin/platform
        String role = user.getRole();
        if (path.startsWith("/platform/") && !"admin".equals(role) && !"platform".equals(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        if (path.startsWith("/m/") && !"shop".equals(role) && !"admin".equals(role) && !"platform".equals(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 将用户信息注入请求头，下游服务通过 @RequestHeader 获取
        ServerHttpRequest req = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(user.getUserId()))
                .header("X-Username", user.getUsername())
                .header("X-Role", user.getRole())
                .header("X-Category", user.getCategory() != null ? user.getCategory() : "")
                .build();

        return chain.filter(exchange.mutate().request(req).build());
    }

    @Override public int getOrder() { return -100; }
}
