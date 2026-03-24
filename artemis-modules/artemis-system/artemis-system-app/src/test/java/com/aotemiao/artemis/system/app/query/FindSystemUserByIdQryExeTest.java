package com.aotemiao.artemis.system.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindSystemUserByIdQryExeTest {

    @Mock
    private SystemUserGateway systemUserGateway;

    @InjectMocks
    private FindSystemUserByIdQryExe findSystemUserByIdQryExe;

    @Test
    void execute_delegatesToGateway() {
        SystemUser systemUser = new SystemUser();
        systemUser.setId(1L);
        systemUser.setUsername("admin");
        when(systemUserGateway.findById(1L)).thenReturn(Optional.of(systemUser));

        Optional<SystemUser> result = findSystemUserByIdQryExe.execute(new FindSystemUserByIdQry(1L));

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
        verify(systemUserGateway).findById(1L);
    }
}
