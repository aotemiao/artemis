package com.aotemiao.artemis.system.app.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
class UpdateSystemRoleCmdExeTest {

    @Mock
    private SystemRoleGateway systemRoleGateway;

    @InjectMocks
    private UpdateSystemRoleCmdExe updateSystemRoleCmdExe;

    @Test
    void execute_whenRoleExists_updatesRole() {
        SystemRole existing = new SystemRole();
        existing.setId(1L);
        existing.setRoleKey("super-admin");
        existing.setRoleName("超级管理员");
        existing.setEnabled(true);
        when(systemRoleGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(systemRoleGateway.findByRoleKey("system-admin")).thenReturn(Optional.empty());
        when(systemRoleGateway.findByRoleName("系统管理员")).thenReturn(Optional.empty());
        when(systemRoleGateway.save(existing)).thenReturn(existing);

        SystemRole result = updateSystemRoleCmdExe.execute(new UpdateSystemRoleCmd(1L, "system-admin", "系统管理员", false));

        assertThat(result.getRoleKey()).isEqualTo("system-admin");
        assertThat(result.getRoleName()).isEqualTo("系统管理员");
        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    void execute_whenAnotherRoleUsesSameKey_throwsBizException() {
        SystemRole existing = new SystemRole();
        existing.setId(1L);
        existing.setRoleKey("super-admin");
        existing.setRoleName("超级管理员");
        SystemRole duplicated = new SystemRole();
        duplicated.setId(2L);
        duplicated.setRoleKey("auditor");
        when(systemRoleGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(systemRoleGateway.findByRoleKey("auditor")).thenReturn(Optional.of(duplicated));

        assertThatThrownBy(() -> updateSystemRoleCmdExe.execute(new UpdateSystemRoleCmd(1L, "auditor", "超级管理员", true)))
                .isInstanceOf(BizException.class);
    }
}
