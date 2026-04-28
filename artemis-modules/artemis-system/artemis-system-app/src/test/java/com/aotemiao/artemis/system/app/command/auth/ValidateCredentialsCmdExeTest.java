package com.aotemiao.artemis.system.app.command.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.auth.UserCredentialsGateway;
import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidateCredentialsCmdExeTest {

    @Mock
    private UserCredentialsGateway userCredentialsGateway;

    @Mock
    private SystemClientGateway systemClientGateway;

    @InjectMocks
    private ValidateCredentialsCmdExe validateCredentialsCmdExe;

    @Test
    void execute_delegatesToGateway_andReturnsUserId() {
        ValidateCredentialsCmd cmd = new ValidateCredentialsCmd("admin", "123456");
        when(systemClientGateway.findByClientId("artemis-admin")).thenReturn(Optional.of(sampleClient("NORMAL")));
        when(userCredentialsGateway.validate("admin", "123456")).thenReturn(Optional.of(1L));

        Optional<Long> result = validateCredentialsCmdExe.execute(cmd);

        verify(userCredentialsGateway).validate("admin", "123456");
        assertThat(result).contains(1L);
    }

    @Test
    void execute_whenClientDisabled_returnsEmpty() {
        ValidateCredentialsCmd cmd = new ValidateCredentialsCmd("artemis-admin", "password", "admin", "123456");
        when(systemClientGateway.findByClientId("artemis-admin")).thenReturn(Optional.of(sampleClient("DISABLED")));

        Optional<Long> result = validateCredentialsCmdExe.execute(cmd);

        assertThat(result).isEmpty();
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
