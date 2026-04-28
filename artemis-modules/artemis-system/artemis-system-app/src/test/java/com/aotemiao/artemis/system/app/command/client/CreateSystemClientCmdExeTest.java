package com.aotemiao.artemis.system.app.command.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSystemClientCmdExeTest {

    @Mock
    private SystemClientGateway systemClientGateway;

    @InjectMocks
    private CreateSystemClientCmdExe createSystemClientCmdExe;

    @Test
    void execute_whenUnique_createsClient() {
        when(systemClientGateway.findByClientId("artemis-admin")).thenReturn(Optional.empty());
        when(systemClientGateway.findByClientKey("artemis-admin-web")).thenReturn(Optional.empty());
        when(systemClientGateway.save(any(SystemClient.class))).thenReturn(sampleClient(1L));

        SystemClient result = createSystemClientCmdExe.execute(sampleCreateCmd());

        assertThat(result.getClientId()).isEqualTo("artemis-admin");
    }

    @Test
    void execute_whenClientIdDuplicated_throwsBizException() {
        when(systemClientGateway.findByClientId("artemis-admin")).thenReturn(Optional.of(sampleClient(1L)));

        assertThatThrownBy(() -> createSystemClientCmdExe.execute(sampleCreateCmd()))
                .isInstanceOf(BizException.class);
    }

    static CreateSystemClientCmd sampleCreateCmd() {
        return new CreateSystemClientCmd(
                "artemis-admin", "artemis-admin-web", "change-me", "password", "PC", 1800L, 86400L, "NORMAL", null);
    }

    static SystemClient sampleClient(Long id) {
        SystemClient client = new SystemClient();
        client.setId(id);
        client.setClientId("artemis-admin");
        client.setClientKey("artemis-admin-web");
        client.setClientSecret("change-me");
        client.setGrantTypes("password");
        client.setDeviceType("PC");
        client.setActiveTimeoutSeconds(1800L);
        client.setFixedTimeoutSeconds(86400L);
        client.setStatus("NORMAL");
        return client;
    }
}
