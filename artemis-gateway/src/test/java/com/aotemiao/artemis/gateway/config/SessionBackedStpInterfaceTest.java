package com.aotemiao.artemis.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.aotemiao.artemis.gateway.support.SaTokenTestSupport;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionBackedStpInterfaceTest {

    private final SessionBackedStpInterface sessionBackedStpInterface = new SessionBackedStpInterface();

    private SaTokenContext previousSaTokenContext;

    @BeforeEach
    void setUp() {
        previousSaTokenContext = SaTokenTestSupport.installContext();
    }

    @AfterEach
    void tearDown() {
        SaManager.setSaTokenContext(previousSaTokenContext);
    }

    @Test
    void getRoleList_whenSessionContainsRoleKeys_shouldReturnRoleKeys() {
        StpUtil.login(1L);
        StpUtil.getSessionByLoginId(1L).set(SaSession.ROLE_LIST, List.of("super-admin", "auditor"));

        assertThat(sessionBackedStpInterface.getRoleList(1L, StpUtil.getLoginType()))
                .containsExactly("super-admin", "auditor");
    }

    @Test
    void getRoleList_whenSessionMissing_shouldReturnEmptyList() {
        assertThat(sessionBackedStpInterface.getRoleList(99L, StpUtil.getLoginType()))
                .isEmpty();
    }
}
