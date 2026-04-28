package com.aotemiao.artemis.system.app.query.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSystemConfigValueQryExeTest {

    @Mock
    private SystemConfigCache systemConfigCache;

    @InjectMocks
    private GetSystemConfigValueQryExe getSystemConfigValueQryExe;

    @Test
    void execute_returnsCachedValue() {
        when(systemConfigCache.getValue("sys.account.registerUser")).thenReturn(Optional.of("false"));

        Optional<String> result =
                getSystemConfigValueQryExe.execute(new GetSystemConfigValueQry("sys.account.registerUser"));

        assertThat(result).contains("false");
    }
}
