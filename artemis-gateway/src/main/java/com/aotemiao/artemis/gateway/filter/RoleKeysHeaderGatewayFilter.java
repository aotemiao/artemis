package com.aotemiao.artemis.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import com.aotemiao.artemis.gateway.config.SaTokenGatewayConfig;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 鉴权通过后，向转发请求注入角色键头，供下游记录或做补充判断。 */
@Component
public class RoleKeysHeaderGatewayFilter implements GlobalFilter, Ordered {

    public static final String X_ROLE_KEYS = "X-Role-Keys";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (SaTokenGatewayConfig.isWhitelistPath(path)) {
            return chain.filter(exchange);
        }
        if (!StpUtil.isLogin()) {
            return chain.filter(exchange);
        }

        List<String> roleKeys = StpUtil.getRoleList();
        if (!roleKeys.isEmpty()) {
            exchange = exchange.mutate()
                    .request(request -> request.header(X_ROLE_KEYS, String.join(",", roleKeys)))
                    .build();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 110;
    }
}
