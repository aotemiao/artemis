package com.aotemiao.artemis.system.app.query.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.user.SystemUserGateway;
import com.aotemiao.artemis.system.domain.gateway.user.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import com.aotemiao.artemis.system.domain.model.user.SystemUser;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListUserRolesQryExeTest {

    @Mock
    private SystemUserGateway systemUserGateway;

    @Mock
    private UserRoleBindingGateway userRoleBindingGateway;

    @InjectMocks
    private ListUserRolesQryExe listUserRolesQryExe;

    @Test
    void execute_returnsRolesWhenUserExists() {
        SystemUser user = new SystemUser();
        user.setId(1L);
        SystemRole role = new SystemRole();
        role.setId(10L);
        role.setRoleKey("super-admin");
        when(systemUserGateway.findById(1L)).thenReturn(Optional.of(user));
        when(userRoleBindingGateway.findRolesByUserId(1L)).thenReturn(List.of(role));

        List<SystemRole> roles = listUserRolesQryExe.execute(new ListUserRolesQry(1L));

        assertThat(roles).containsExactly(role);
    }
}
