package com.aotemiao.artemis.gateway.config;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

/** 网关最小 RBAC 策略：阻断内部接口，并为高风险管理路由要求管理员角色。 */
@Component
public class GatewayAuthorizationPolicy {

    static final String SUPER_ADMIN_ROLE = "super-admin";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> BLOCKED_EXTERNAL_PATHS = List.of("/api/system/internal/**");
    private static final List<String> SUPER_ADMIN_PATHS = List.of("/api/system/users/**", "/api/system/roles/**");

    public void checkBeforeLogin(String path) {
        if (blocksByGateway(path)) {
            throw new NotPermissionException("gateway-internal", StpUtil.getLoginType());
        }
    }

    public void checkAfterLogin(String path) {
        if (requiresSuperAdmin(path) && !StpUtil.hasRole(SUPER_ADMIN_ROLE)) {
            throw new NotRoleException(SUPER_ADMIN_ROLE, StpUtil.getLoginType());
        }
    }

    boolean blocksByGateway(String path) {
        return matchesAny(BLOCKED_EXTERNAL_PATHS, path);
    }

    boolean requiresSuperAdmin(String path) {
        return matchesAny(SUPER_ADMIN_PATHS, path);
    }

    private boolean matchesAny(List<String> patterns, String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return patterns.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}
