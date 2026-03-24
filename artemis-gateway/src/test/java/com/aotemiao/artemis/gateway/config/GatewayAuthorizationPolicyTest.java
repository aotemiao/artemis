package com.aotemiao.artemis.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.aotemiao.artemis.gateway.support.SaTokenTestSupport;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GatewayAuthorizationPolicyTest {

    private final GatewayAuthorizationPolicy gatewayAuthorizationPolicy = new GatewayAuthorizationPolicy();

    private SaTokenContext previousSaTokenContext;
    private StpInterface previousStpInterface;

    @BeforeEach
    void setUp() {
        previousSaTokenContext = SaTokenTestSupport.installContext();
        previousStpInterface = SaManager.getStpInterface();
        SaManager.setStpInterface(new SessionBackedStpInterface());
    }

    @AfterEach
    void tearDown() {
        SaManager.setSaTokenContext(previousSaTokenContext);
        SaManager.setStpInterface(previousStpInterface);
    }

    @Test
    void blocksByGateway_shouldReturnTrueForInternalRoute() {
        assertThat(gatewayAuthorizationPolicy.blocksByGateway("/api/system/internal/auth/users/1/authorization"))
                .isTrue();
    }

    @Test
    void requiresSuperAdmin_shouldReturnTrueForManagementRoutes() {
        assertThat(gatewayAuthorizationPolicy.requiresSuperAdmin("/api/system/users/1"))
                .isTrue();
        assertThat(gatewayAuthorizationPolicy.requiresSuperAdmin("/api/system/roles/1"))
                .isTrue();
        assertThat(gatewayAuthorizationPolicy.requiresSuperAdmin("/api/system/lookup/types"))
                .isFalse();
    }

    @Test
    void checkAfterLogin_whenRoleMissing_shouldThrowNotRoleException() {
        StpUtil.login(101L);

        assertThatThrownBy(() -> gatewayAuthorizationPolicy.checkAfterLogin("/api/system/users/1"))
                .isInstanceOf(NotRoleException.class);
    }

    @Test
    void checkAfterLogin_whenRolePresent_shouldAllowProtectedRoute() {
        StpUtil.login(102L);
        StpUtil.getSessionByLoginId(102L)
                .set(SaSession.ROLE_LIST, List.of(GatewayAuthorizationPolicy.SUPER_ADMIN_ROLE));

        assertThatCode(() -> gatewayAuthorizationPolicy.checkAfterLogin("/api/system/users/1"))
                .doesNotThrowAnyException();
    }
}
