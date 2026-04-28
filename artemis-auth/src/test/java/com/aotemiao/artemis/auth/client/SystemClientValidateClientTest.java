package com.aotemiao.artemis.auth.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.client.api.SystemClientValidateService;
import com.aotemiao.artemis.system.client.dto.ValidateClientRequest;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemClientValidateClientTest {

    @Mock
    private SystemClientValidateService systemClientValidateService;

    private SystemClientValidateClient systemClientValidateClient;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        systemClientValidateClient = new SystemClientValidateClient();
        Field field = SystemClientValidateClient.class.getDeclaredField("systemClientValidateService");
        field.setAccessible(true);
        field.set(systemClientValidateClient, systemClientValidateService);
    }

    @Test
    void validate_delegatesToDubboService() {
        when(systemClientValidateService.validate(new ValidateClientRequest("artemis-admin", "password")))
                .thenReturn(true);

        boolean result = systemClientValidateClient.validate("artemis-admin", "password");

        ArgumentCaptor<ValidateClientRequest> captor = ArgumentCaptor.forClass(ValidateClientRequest.class);
        verify(systemClientValidateService).validate(captor.capture());
        assertThat(captor.getValue().clientId()).isEqualTo("artemis-admin");
        assertThat(captor.getValue().grantType()).isEqualTo("password");
        assertThat(result).isTrue();
    }
}
