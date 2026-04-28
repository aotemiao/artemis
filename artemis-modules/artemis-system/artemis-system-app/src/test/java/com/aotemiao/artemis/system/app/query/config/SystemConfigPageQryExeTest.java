package com.aotemiao.artemis.system.app.query.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemConfigPageQryExeTest {

    @Mock
    private SystemConfigGateway systemConfigGateway;

    @InjectMocks
    private SystemConfigPageQryExe systemConfigPageQryExe;

    @Test
    void execute_returnsGatewayPage() {
        PageRequest pageRequest = new PageRequest(0, 10);
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setConfigKey("sys.account.registerUser");
        when(systemConfigGateway.findPage(pageRequest)).thenReturn(PageResult.of(1, List.of(systemConfig), 1));

        PageResult<SystemConfig> result = systemConfigPageQryExe.execute(new SystemConfigPageQry(pageRequest));

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.content().getFirst().getConfigKey()).isEqualTo("sys.account.registerUser");
    }
}
