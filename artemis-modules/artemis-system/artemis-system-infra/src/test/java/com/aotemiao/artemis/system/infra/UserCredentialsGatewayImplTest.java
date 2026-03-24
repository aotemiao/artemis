package com.aotemiao.artemis.system.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.infra.dataobject.SystemUserDO;
import com.aotemiao.artemis.system.infra.gateway.UserCredentialsGatewayImpl;
import com.aotemiao.artemis.system.infra.repository.SystemUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCredentialsGatewayImplTest {

    @Mock
    private SystemUserRepository systemUserRepository;

    @InjectMocks
    private UserCredentialsGatewayImpl userCredentialsGateway;

    @Test
    void validate_whenCredentialsMatch_returnsUserId() {
        SystemUserDO systemUserDO = new SystemUserDO();
        systemUserDO.setId(1L);
        systemUserDO.setUsername("admin");
        systemUserDO.setDisplayName("管理员");
        systemUserDO.setPassword("123456");
        systemUserDO.setEnabled(true);
        when(systemUserRepository.findByUsernameAndDeleted("admin", 0)).thenReturn(Optional.of(systemUserDO));

        assertThat(userCredentialsGateway.validate("admin", "123456")).contains(1L);
    }

    @Test
    void validate_whenCredentialsDoNotMatch_returnsEmpty() {
        SystemUserDO systemUserDO = new SystemUserDO();
        systemUserDO.setId(1L);
        systemUserDO.setUsername("admin");
        systemUserDO.setDisplayName("管理员");
        systemUserDO.setPassword("123456");
        systemUserDO.setEnabled(true);
        when(systemUserRepository.findByUsernameAndDeleted("admin", 0)).thenReturn(Optional.of(systemUserDO));

        assertThat(userCredentialsGateway.validate("admin", "wrong")).isEmpty();
    }

    @Test
    void validate_whenUserDisabled_returnsEmpty() {
        SystemUserDO systemUserDO = new SystemUserDO();
        systemUserDO.setId(1L);
        systemUserDO.setUsername("admin");
        systemUserDO.setDisplayName("管理员");
        systemUserDO.setPassword("123456");
        systemUserDO.setEnabled(false);
        when(systemUserRepository.findByUsernameAndDeleted("admin", 0)).thenReturn(Optional.of(systemUserDO));

        assertThat(userCredentialsGateway.validate("admin", "123456")).isEmpty();
    }

    @Test
    void validate_whenInputInvalid_returnsEmpty() {
        assertThat(userCredentialsGateway.validate(null, "123456")).isEmpty();
    }
}
