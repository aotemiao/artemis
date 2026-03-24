package com.aotemiao.artemis.auth.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.client.api.UserAuthorizationService;
import com.aotemiao.artemis.system.client.dto.UserAuthorizationSnapshotDTO;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemUserAuthorizationClientTest {

    @Mock
    private UserAuthorizationService userAuthorizationService;

    private SystemUserAuthorizationClient systemUserAuthorizationClient;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        systemUserAuthorizationClient = new SystemUserAuthorizationClient();
        Field field = SystemUserAuthorizationClient.class.getDeclaredField("userAuthorizationService");
        field.setAccessible(true);
        field.set(systemUserAuthorizationClient, userAuthorizationService);
    }

    @Test
    void getByUserId_delegatesToDubboService_andReturnsResult() {
        UserAuthorizationSnapshotDTO snapshot =
                new UserAuthorizationSnapshotDTO(1L, "admin", "管理员", List.of("super-admin"));
        when(userAuthorizationService.getByUserId(1L)).thenReturn(Optional.of(snapshot));

        Optional<UserAuthorizationSnapshotDTO> result = systemUserAuthorizationClient.getByUserId(1L);

        verify(userAuthorizationService).getByUserId(1L);
        assertThat(result).contains(snapshot);
    }
}
