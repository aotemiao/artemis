package com.aotemiao.artemis.system.app.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.UserCredentialsGateway;
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

    @InjectMocks
    private ValidateCredentialsCmdExe validateCredentialsCmdExe;

    @Test
    void execute_delegatesToGateway_andReturnsUserId() {
        ValidateCredentialsCmd cmd = new ValidateCredentialsCmd("admin", "123456");
        when(userCredentialsGateway.validate("admin", "123456")).thenReturn(Optional.of(1L));

        Optional<Long> result = validateCredentialsCmdExe.execute(cmd);

        verify(userCredentialsGateway).validate("admin", "123456");
        assertThat(result).contains(1L);
    }
}
