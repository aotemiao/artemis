package com.aotemiao.artemis.system.app.command.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateSystemConfigValueCmdExeTest {

    @Mock
    private SystemConfigGateway systemConfigGateway;

    @Mock
    private SystemConfigCache systemConfigCache;

    @InjectMocks
    private UpdateSystemConfigValueCmdExe updateSystemConfigValueCmdExe;

    @Test
    void execute_whenConfigExists_updatesValueByKey() {
        SystemConfig existing = sampleConfig();
        when(systemConfigGateway.findByConfigKey("sys.account.registerUser")).thenReturn(Optional.of(existing));
        when(systemConfigGateway.save(any(SystemConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemConfig result = updateSystemConfigValueCmdExe.execute(
                new UpdateSystemConfigValueCmd("sys.account.registerUser", "true"));

        assertThat(result.getConfigValue()).isEqualTo("true");
        verify(systemConfigCache).evict("sys.account.registerUser");
    }

    private static SystemConfig sampleConfig() {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setId(1L);
        systemConfig.setConfigName("账号注册开关");
        systemConfig.setConfigKey("sys.account.registerUser");
        systemConfig.setConfigValue("false");
        systemConfig.setSystemBuiltIn(true);
        return systemConfig;
    }
}
