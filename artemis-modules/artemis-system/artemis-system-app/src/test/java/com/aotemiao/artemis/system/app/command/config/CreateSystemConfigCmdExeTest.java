package com.aotemiao.artemis.system.app.command.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
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
class CreateSystemConfigCmdExeTest {

    @Mock
    private SystemConfigGateway systemConfigGateway;

    @Mock
    private SystemConfigCache systemConfigCache;

    @InjectMocks
    private CreateSystemConfigCmdExe createSystemConfigCmdExe;

    @Test
    void execute_whenConfigKeyAvailable_createsConfig() {
        CreateSystemConfigCmd cmd =
                new CreateSystemConfigCmd("账号注册开关", "sys.account.registerUser", "false", true, "是否允许注册");
        SystemConfig saved = sampleConfig(1L, "sys.account.registerUser", "false", true);
        when(systemConfigGateway.findByConfigKey("sys.account.registerUser")).thenReturn(Optional.empty());
        when(systemConfigGateway.save(any(SystemConfig.class))).thenReturn(saved);

        SystemConfig result = createSystemConfigCmdExe.execute(cmd);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getConfigKey()).isEqualTo("sys.account.registerUser");
        verify(systemConfigGateway).save(any(SystemConfig.class));
        verify(systemConfigCache).evict("sys.account.registerUser");
    }

    @Test
    void execute_whenConfigKeyExists_throwsBizException() {
        CreateSystemConfigCmd cmd = new CreateSystemConfigCmd("账号注册开关", "sys.account.registerUser", "true", true, null);
        when(systemConfigGateway.findByConfigKey("sys.account.registerUser"))
                .thenReturn(Optional.of(sampleConfig(1L, "sys.account.registerUser", "false", true)));

        assertThatThrownBy(() -> createSystemConfigCmdExe.execute(cmd)).isInstanceOf(BizException.class);
    }

    private static SystemConfig sampleConfig(Long id, String configKey, String configValue, boolean builtIn) {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setId(id);
        systemConfig.setConfigName("参数");
        systemConfig.setConfigKey(configKey);
        systemConfig.setConfigValue(configValue);
        systemConfig.setSystemBuiltIn(builtIn);
        return systemConfig;
    }
}
