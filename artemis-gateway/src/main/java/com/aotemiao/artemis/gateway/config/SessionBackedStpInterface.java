package com.aotemiao.artemis.gateway.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import java.util.List;
import org.springframework.stereotype.Component;

/** 从登录会话中读取最小角色信息，供 gateway 的 `checkRole` / `hasRole` 复用。 */
@Component
public class SessionBackedStpInterface implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            return List.of();
        }
        Object roleList = session.get(SaSession.ROLE_LIST);
        if (!(roleList instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }
}
