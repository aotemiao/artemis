package com.aotemiao.artemis.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import com.aotemiao.artemis.gateway.config.SaTokenGatewayConfig;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 鉴权通过后，向转发请求注入 X-User-Id，供下游读取当前用户。
 * 仅对非白名单路径生效（白名单请求不经过鉴权，无 loginId）。
 */
@Component
public class UserIdHeaderGatewayFilter implements GlobalFilter, Ordered {

    public static final String X_USER_ID = "X-User-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (SaTokenGatewayConfig.isWhitelistPath(path)) {
            return chain.filter(exchange);
        }
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                exchange = exchange.mutate()
                        .request(r -> r.header(X_USER_ID, String.valueOf(loginId)))
                        .build();
            }
        } catch (Exception ignored) {
            // 未登录时由 SaReactorFilter 已返回 401，此处不重复处理
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
