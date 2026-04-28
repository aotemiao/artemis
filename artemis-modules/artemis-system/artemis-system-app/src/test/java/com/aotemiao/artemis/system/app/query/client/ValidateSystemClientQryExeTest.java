package com.aotemiao.artemis.system.app.query.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidateSystemClientQryExeTest {

    @Mock
    private SystemClientGateway systemClientGateway;

    @InjectMocks
    private ValidateSystemClientQryExe validateSystemClientQryExe;

    @Test
    void execute_whenClientNormalAndGrantTypeAllowed_returnsTrue() {
        when(systemClientGateway.findByClientId("artemis-admin")).thenReturn(Optional.of(sampleClient("NORMAL")));

        boolean result = validateSystemClientQryExe.execute(new ValidateSystemClientQry("artemis-admin", "password"));

        assertThat(result).isTrue();
    }

    @Test
    void execute_whenGrantTypeNotAllowed_returnsFalse() {
        when(systemClientGateway.findByClientId("artemis-admin")).thenReturn(Optional.of(sampleClient("NORMAL")));

        boolean result = validateSystemClientQryExe.execute(new ValidateSystemClientQry("artemis-admin", "sms"));

        assertThat(result).isFalse();
    }

    private static SystemClient sampleClient(String status) {
        SystemClient client = new SystemClient();
        client.setId(1L);
        client.setClientId("artemis-admin");
        client.setGrantTypes("password,refresh_token");
        client.setStatus(status);
        return client;
    }
}
