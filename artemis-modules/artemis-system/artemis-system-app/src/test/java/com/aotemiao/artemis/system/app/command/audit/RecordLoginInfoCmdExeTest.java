package com.aotemiao.artemis.system.app.command.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.audit.LoginInfoGateway;
import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordLoginInfoCmdExeTest {

    @Mock
    private LoginInfoGateway loginInfoGateway;

    @InjectMocks
    private RecordLoginInfoCmdExe recordLoginInfoCmdExe;

    @Test
    void execute_recordsLoginInfoWithDefaults() {
        when(loginInfoGateway.save(any(LoginInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginInfo result = recordLoginInfoCmdExe.execute(new RecordLoginInfoCmd(
                null, "admin", "artemis-admin", null, "127.0.0.1", null, "Chrome", "Windows", "SUCCESS", "登录成功"));

        assertThat(result.getTenantId()).isEqualTo("000000");
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getClientId()).isEqualTo("artemis-admin");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getLoginTime()).isNotNull();
    }
}
