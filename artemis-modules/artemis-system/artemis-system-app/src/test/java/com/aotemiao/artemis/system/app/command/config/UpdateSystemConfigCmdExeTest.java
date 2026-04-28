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
class UpdateSystemConfigCmdExeTest {

    @Mock
    private SystemConfigGateway systemConfigGateway;

    @Mock
    private SystemConfigCache systemConfigCache;

    @InjectMocks
    private UpdateSystemConfigCmdExe updateSystemConfigCmdExe;

    @Test
    void execute_whenConfigExists_updatesAndEvictsCache() {
        SystemConfig existing = sampleConfig(1L, "sys.user.initPassword", "123456", true);
        SystemConfig saved = sampleConfig(1L, "sys.user.initialPassword", "abc123", true);
        when(systemConfigGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(systemConfigGateway.findByConfigKey("sys.user.initialPassword")).thenReturn(Optional.empty());
        when(systemConfigGateway.save(any(SystemConfig.class))).thenReturn(saved);

        SystemConfig result = updateSystemConfigCmdExe.execute(
                new UpdateSystemConfigCmd(1L, "用户初始密码", "sys.user.initialPassword", "abc123", true, "默认密码"));

        assertThat(result.getConfigValue()).isEqualTo("abc123");
        verify(systemConfigCache).evict("sys.user.initPassword");
        verify(systemConfigCache).evict("sys.user.initialPassword");
    }

    @Test
    void execute_whenConfigKeyBelongsToOtherConfig_throwsBizException() {
        SystemConfig existing = sampleConfig(1L, "sys.user.initPassword", "123456", true);
        SystemConfig duplicated = sampleConfig(2L, "sys.account.registerUser", "false", true);
        when(systemConfigGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(systemConfigGateway.findByConfigKey("sys.account.registerUser")).thenReturn(Optional.of(duplicated));

        assertThatThrownBy(() -> updateSystemConfigCmdExe.execute(
                        new UpdateSystemConfigCmd(1L, "用户初始密码", "sys.account.registerUser", "abc123", true, null)))
                .isInstanceOf(BizException.class);
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
