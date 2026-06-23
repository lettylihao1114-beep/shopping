package com.gec.shop.order.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Day03 6.11：Sentinel 授权规则 — 定义请求来源如何获取
 *
 * 要求客户端在请求参数中携带 serviceName 作为来源标识
 * 比如：/auth1?serviceName=pc  → 来源 = "pc"
 *       /auth1?serviceName=app → 来源 = "app"
 */
@Component
public class RequestOriginParserDefinition implements RequestOriginParser {

    @Override
    public String parseOrigin(HttpServletRequest request) {
        // 从 URL 参数中获取 serviceName 作为请求来源
        String serviceName = request.getParameter("serviceName");
        return serviceName;
    }
}
