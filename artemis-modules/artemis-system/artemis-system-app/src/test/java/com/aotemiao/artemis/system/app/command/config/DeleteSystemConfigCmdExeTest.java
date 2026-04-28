package com.aotemiao.artemis.system.app.command.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
class DeleteSystemConfigCmdExeTest {

    @Mock
    private SystemConfigGateway systemConfigGateway;

    @Mock
    private SystemConfigCache systemConfigCache;

    @InjectMocks
    private DeleteSystemConfigCmdExe deleteSystemConfigCmdExe;

    @Test
    void execute_whenConfigIsNotBuiltIn_deletesAndEvictsCache() {
        SystemConfig systemConfig = sampleConfig(false);
        when(systemConfigGateway.findById(1L)).thenReturn(Optional.of(systemConfig));

        deleteSystemConfigCmdExe.execute(new DeleteSystemConfigCmd(1L));

        verify(systemConfigGateway).deleteById(1L);
        verify(systemConfigCache).evict("demo.flag");
    }

    @Test
    void execute_whenConfigIsBuiltIn_throwsBizException() {
        when(systemConfigGateway.findById(1L)).thenReturn(Optional.of(sampleConfig(true)));

        assertThatThrownBy(() -> deleteSystemConfigCmdExe.execute(new DeleteSystemConfigCmd(1L)))
                .isInstanceOf(BizException.class);
    }

    private static SystemConfig sampleConfig(boolean builtIn) {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setId(1L);
        systemConfig.setConfigName("演示开关");
        systemConfig.setConfigKey("demo.flag");
        systemConfig.setConfigValue("true");
        systemConfig.setSystemBuiltIn(builtIn);
        return systemConfig;
    }
}
