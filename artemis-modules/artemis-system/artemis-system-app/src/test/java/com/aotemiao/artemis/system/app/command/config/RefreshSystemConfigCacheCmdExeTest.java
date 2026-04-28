package com.aotemiao.artemis.system.app.command.config;

import static org.mockito.Mockito.verify;

import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshSystemConfigCacheCmdExeTest {

    @Mock
    private SystemConfigCache systemConfigCache;

    @InjectMocks
    private RefreshSystemConfigCacheCmdExe refreshSystemConfigCacheCmdExe;

    @Test
    void execute_refreshesCache() {
        refreshSystemConfigCacheCmdExe.execute(new RefreshSystemConfigCacheCmd());

        verify(systemConfigCache).refresh();
    }
}
