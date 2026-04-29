package com.aotemiao.artemis.system.app.command.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.app.service.tenant.TenantRuntimeService;
import com.aotemiao.artemis.system.domain.gateway.user.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.user.SystemUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserCmdExeTest {

    @Mock
    private SystemUserGateway systemUserGateway;

    @Mock
    private SystemConfigCache systemConfigCache;

    @Mock
    private TenantRuntimeService tenantRuntimeService;

    @InjectMocks
    private RegisterUserCmdExe registerUserCmdExe;

    @Test
    void execute_whenRegisterEnabled_createsUser() {
        when(systemConfigCache.getValue("sys.account.registerUser")).thenReturn(Optional.of("true"));
        when(tenantRuntimeService.normalizeTenantNo("000000")).thenReturn("000000");
        when(systemUserGateway.findByUsername("demo")).thenReturn(Optional.empty());
        when(systemUserGateway.save(any(SystemUser.class))).thenReturn(savedUser());

        Long userId = registerUserCmdExe.execute(sampleCmd("SYSTEM"));

        assertThat(userId).isEqualTo(2L);
    }

    @Test
    void execute_whenRegisterDisabled_throwsBizException() {
        when(systemConfigCache.getValue("sys.account.registerUser")).thenReturn(Optional.of("false"));

        assertThatThrownBy(() -> registerUserCmdExe.execute(sampleCmd("SYSTEM")))
                .isInstanceOf(BizException.class);
    }

    @Test
    void execute_whenUserTypeUnsupported_throwsBizException() {
        when(systemConfigCache.getValue("sys.account.registerUser")).thenReturn(Optional.of("true"));

        assertThatThrownBy(() -> registerUserCmdExe.execute(sampleCmd("TENANT_ADMIN")))
                .isInstanceOf(BizException.class);
    }

    private static RegisterUserCmd sampleCmd(String userType) {
        return new RegisterUserCmd("000000", "artemis-admin", "password", "demo", "123456", userType);
    }

    private static SystemUser savedUser() {
        SystemUser user = new SystemUser();
        user.setId(2L);
        user.setUsername("demo");
        user.setDisplayName("demo");
        user.setPassword("123456");
        user.setEnabled(true);
        return user;
    }
}
