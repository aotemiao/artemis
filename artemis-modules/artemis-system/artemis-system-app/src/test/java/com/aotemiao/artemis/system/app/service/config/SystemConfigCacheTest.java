package com.aotemiao.artemis.system.app.service.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemConfigCacheTest {

    @Mock
    private SystemConfigGateway systemConfigGateway;

    @Test
    void getValue_cachesGatewayResultUntilEvicted() {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setConfigKey("sys.account.registerUser");
        systemConfig.setConfigValue("false");
        when(systemConfigGateway.findByConfigKey("sys.account.registerUser")).thenReturn(Optional.of(systemConfig));
        SystemConfigCache cache = new SystemConfigCache(systemConfigGateway);

        assertThat(cache.getValue("sys.account.registerUser")).contains("false");
        assertThat(cache.getValue("sys.account.registerUser")).contains("false");
        verify(systemConfigGateway, times(1)).findByConfigKey("sys.account.registerUser");

        cache.evict("sys.account.registerUser");
        assertThat(cache.getValue("sys.account.registerUser")).contains("false");
        verify(systemConfigGateway, times(2)).findByConfigKey("sys.account.registerUser");
    }
}
