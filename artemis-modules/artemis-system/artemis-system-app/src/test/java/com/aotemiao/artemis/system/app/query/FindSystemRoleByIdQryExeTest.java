package com.aotemiao.artemis.system.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindSystemRoleByIdQryExeTest {

    @Mock
    private SystemRoleGateway systemRoleGateway;

    @InjectMocks
    private FindSystemRoleByIdQryExe findSystemRoleByIdQryExe;

    @Test
    void execute_returnsRoleWhenFound() {
        SystemRole role = new SystemRole();
        role.setId(1L);
        role.setRoleKey("super-admin");
        when(systemRoleGateway.findById(1L)).thenReturn(Optional.of(role));

        Optional<SystemRole> result = findSystemRoleByIdQryExe.execute(new FindSystemRoleByIdQry(1L));

        assertThat(result).contains(role);
    }
}
