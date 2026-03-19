package com.aotemiao.artemis.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 网关 Sa-Token 鉴权配置：白名单放行，非白名单校验登录。 */
@Configuration
public class SaTokenGatewayConfig {

    /** 白名单路径前缀，不校验 Token */
    private static final String[] AUTH_WHITELIST_PREFIXES = {"/auth/", "/public/"};

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude("/auth/**", "/public/**")
                .setAuth(r -> StpUtil.checkLogin());
    }

    /** 判断路径是否在白名单内（用于其他过滤器）。 */
    public static boolean isWhitelistPath(String path) {
        if (path == null) return true;
        for (String prefix : AUTH_WHITELIST_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}
