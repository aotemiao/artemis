package com.aotemiao.artemis.auth.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.client.api.UserValidateService;
import com.aotemiao.artemis.system.client.dto.ValidateCredentialsRequest;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemUserValidateClientTest {

    @Mock
    private UserValidateService userValidateService;

    private SystemUserValidateClient systemUserValidateClient;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        systemUserValidateClient = new SystemUserValidateClient();
        Field field = SystemUserValidateClient.class.getDeclaredField("userValidateService");
        field.setAccessible(true);
        field.set(systemUserValidateClient, userValidateService);
    }

    @Test
    void validate_delegatesToDubboService_andReturnsResult() {
        when(userValidateService.validate(new ValidateCredentialsRequest("admin", "123456")))
                .thenReturn(Optional.of(1L));

        Optional<Long> result = systemUserValidateClient.validate("admin", "123456");

        ArgumentCaptor<ValidateCredentialsRequest> captor = ArgumentCaptor.forClass(ValidateCredentialsRequest.class);
        verify(userValidateService).validate(captor.capture());
        assertThat(captor.getValue().username()).isEqualTo("admin");
        assertThat(captor.getValue().password()).isEqualTo("123456");
        assertThat(result).contains(1L);
    }
}
