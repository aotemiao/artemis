package com.aotemiao.artemis.system.app.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.gateway.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReplaceUserRolesCmdExeTest {

    @Mock
    private SystemUserGateway systemUserGateway;

    @Mock
    private SystemRoleGateway systemRoleGateway;

    @Mock
    private UserRoleBindingGateway userRoleBindingGateway;

    @InjectMocks
    private ReplaceUserRolesCmdExe replaceUserRolesCmdExe;

    @Test
    void execute_whenUserAndRolesExist_replacesBindings() {
        SystemUser user = new SystemUser();
        user.setId(1L);
        SystemRole admin = new SystemRole();
        admin.setId(10L);
        admin.setRoleKey("super-admin");
        admin.setRoleName("超级管理员");
        SystemRole auditor = new SystemRole();
        auditor.setId(20L);
        auditor.setRoleKey("auditor");
        when(systemUserGateway.findById(1L)).thenReturn(Optional.of(user));
        when(systemRoleGateway.findByIds(List.of(10L, 20L))).thenReturn(List.of(admin, auditor));
        when(userRoleBindingGateway.findRolesByUserId(1L)).thenReturn(List.of(admin, auditor));

        List<SystemRole> roles = replaceUserRolesCmdExe.execute(new ReplaceUserRolesCmd(1L, List.of(10L, 20L, 20L)));

        assertThat(roles).hasSize(2);
        verify(userRoleBindingGateway).replaceRoles(1L, List.of(10L, 20L));
    }

    @Test
    void execute_whenSomeRolesMissing_throwsBizException() {
        SystemUser user = new SystemUser();
        user.setId(1L);
        when(systemUserGateway.findById(1L)).thenReturn(Optional.of(user));
        when(systemRoleGateway.findByIds(List.of(10L))).thenReturn(List.of());

        assertThatThrownBy(() -> replaceUserRolesCmdExe.execute(new ReplaceUserRolesCmd(1L, List.of(10L))))
                .isInstanceOf(BizException.class);
    }
}
