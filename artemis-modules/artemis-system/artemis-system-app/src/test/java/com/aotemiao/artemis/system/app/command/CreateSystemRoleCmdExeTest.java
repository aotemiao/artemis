package com.aotemiao.artemis.system.app.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSystemRoleCmdExeTest {

    @Mock
    private SystemRoleGateway systemRoleGateway;

    @InjectMocks
    private CreateSystemRoleCmdExe createSystemRoleCmdExe;

    @Test
    void execute_whenRoleKeyAndNameAvailable_createsRole() {
        CreateSystemRoleCmd cmd = new CreateSystemRoleCmd("super-admin", "超级管理员");
        SystemRole saved = new SystemRole();
        saved.setId(1L);
        saved.setRoleKey("super-admin");
        saved.setRoleName("超级管理员");
        saved.setEnabled(true);
        when(systemRoleGateway.findByRoleKey("super-admin")).thenReturn(Optional.empty());
        when(systemRoleGateway.findByRoleName("超级管理员")).thenReturn(Optional.empty());
        when(systemRoleGateway.save(any(SystemRole.class))).thenReturn(saved);

        SystemRole result = createSystemRoleCmdExe.execute(cmd);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRoleKey()).isEqualTo("super-admin");
        verify(systemRoleGateway).save(any(SystemRole.class));
    }

    @Test
    void execute_whenRoleKeyExists_throwsBizException() {
        CreateSystemRoleCmd cmd = new CreateSystemRoleCmd("super-admin", "超级管理员");
        SystemRole existing = new SystemRole();
        existing.setId(1L);
        existing.setRoleKey("super-admin");
        when(systemRoleGateway.findByRoleKey("super-admin")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> createSystemRoleCmdExe.execute(cmd)).isInstanceOf(BizException.class);
    }
}
