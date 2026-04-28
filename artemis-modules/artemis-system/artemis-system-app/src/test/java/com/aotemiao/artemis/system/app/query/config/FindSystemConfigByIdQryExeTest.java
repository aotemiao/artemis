package com.aotemiao.artemis.system.app.query.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindSystemConfigByIdQryExeTest {

    @Mock
    private SystemConfigGateway systemConfigGateway;

    @InjectMocks
    private FindSystemConfigByIdQryExe findSystemConfigByIdQryExe;

    @Test
    void execute_returnsGatewayResult() {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setId(1L);
        when(systemConfigGateway.findById(1L)).thenReturn(Optional.of(systemConfig));

        Optional<SystemConfig> result = findSystemConfigByIdQryExe.execute(new FindSystemConfigByIdQry(1L));

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }
}
