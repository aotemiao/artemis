package com.aotemiao.artemis.system.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.gateway.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import com.aotemiao.artemis.system.domain.model.UserAuthorizationSnapshot;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserAuthorizationQryExeTest {

    @Mock
    private SystemUserGateway systemUserGateway;

    @Mock
    private UserRoleBindingGateway userRoleBindingGateway;

    @InjectMocks
    private GetUserAuthorizationQryExe getUserAuthorizationQryExe;

    @Test
    void execute_returnsUserSnapshotWithEnabledRoleKeys() {
        SystemUser systemUser = new SystemUser();
        systemUser.setId(7L);
        systemUser.setUsername("admin");
        systemUser.setDisplayName("管理员");

        SystemRole adminRole = new SystemRole();
        adminRole.setRoleKey("super-admin");
        adminRole.setEnabled(true);

        SystemRole disabledRole = new SystemRole();
        disabledRole.setRoleKey("auditor");
        disabledRole.setEnabled(false);

        when(systemUserGateway.findById(7L)).thenReturn(Optional.of(systemUser));
        when(userRoleBindingGateway.findRolesByUserId(7L)).thenReturn(List.of(adminRole, disabledRole, adminRole));

        Optional<UserAuthorizationSnapshot> result =
                getUserAuthorizationQryExe.execute(new GetUserAuthorizationQry(7L));

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(7L);
        assertThat(result.get().username()).isEqualTo("admin");
        assertThat(result.get().displayName()).isEqualTo("管理员");
        assertThat(result.get().roleKeys()).containsExactly("super-admin");
        verify(systemUserGateway).findById(7L);
        verify(userRoleBindingGateway).findRolesByUserId(7L);
    }

    @Test
    void execute_whenUserMissing_returnsEmpty() {
        when(systemUserGateway.findById(8L)).thenReturn(Optional.empty());

        Optional<UserAuthorizationSnapshot> result =
                getUserAuthorizationQryExe.execute(new GetUserAuthorizationQry(8L));

        assertThat(result).isEmpty();
        verify(systemUserGateway).findById(8L);
    }
}
