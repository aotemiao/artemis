package com.aotemiao.artemis.gateway.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/** 网关 Sa-Token 鉴权配置：白名单放行，非白名单校验登录。 */
@Configuration
public class SaTokenGatewayConfig {

    /** 白名单路径前缀，不校验 Token */
    private static final String[] AUTH_WHITELIST_PREFIXES = {"/auth/", "/public/"};

    private final GatewayAuthorizationPolicy gatewayAuthorizationPolicy;

    public SaTokenGatewayConfig(GatewayAuthorizationPolicy gatewayAuthorizationPolicy) {
        this.gatewayAuthorizationPolicy = gatewayAuthorizationPolicy;
    }

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude("/auth/**", "/public/**")
                .setAuth(r -> {
                    String path = SaReactorSyncHolder.getContext()
                            .getRequest()
                            .getPath()
                            .value();
                    gatewayAuthorizationPolicy.checkBeforeLogin(path);
                    StpUtil.checkLogin();
                    gatewayAuthorizationPolicy.checkAfterLogin(path);
                })
                .setError(e -> {
                    if (e instanceof NotLoginException) {
                        return SaResult.error("未登录或登录已失效").setCode(HttpStatus.UNAUTHORIZED.value());
                    }
                    if (e instanceof NotRoleException || e instanceof NotPermissionException) {
                        return SaResult.error("没有权限访问该网关路由").setCode(HttpStatus.FORBIDDEN.value());
                    }
                    return SaResult.error("网关鉴权失败").setCode(HttpStatus.UNAUTHORIZED.value());
                });
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
