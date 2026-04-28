package com.aotemiao.artemis.system.app.command.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.role.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.role.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReplaceRoleMenusCmdExeTest {

    @Mock
    private SystemRoleGateway systemRoleGateway;

    @Mock
    private SystemMenuGateway systemMenuGateway;

    @Mock
    private RoleMenuBindingGateway roleMenuBindingGateway;

    @InjectMocks
    private ReplaceRoleMenusCmdExe replaceRoleMenusCmdExe;

    @Test
    void execute_whenRoleAndMenusExist_replacesBindings() {
        SystemRole role = new SystemRole();
        role.setId(1L);
        SystemMenu menu = new SystemMenu();
        menu.setId(10L);
        when(systemRoleGateway.findById(1L)).thenReturn(Optional.of(role));
        when(systemMenuGateway.findByIds(List.of(10L, 20L))).thenReturn(List.of(menu, new SystemMenu()));
        when(roleMenuBindingGateway.findMenusByRoleId(1L)).thenReturn(List.of(menu));

        List<SystemMenu> result = replaceRoleMenusCmdExe.execute(new ReplaceRoleMenusCmd(1L, List.of(10L, 20L, 20L)));

        assertThat(result).containsExactly(menu);
        verify(roleMenuBindingGateway).replaceMenus(1L, List.of(10L, 20L));
    }

    @Test
    void execute_whenSomeMenusMissing_throwsBizException() {
        SystemRole role = new SystemRole();
        role.setId(1L);
        when(systemRoleGateway.findById(1L)).thenReturn(Optional.of(role));
        when(systemMenuGateway.findByIds(List.of(10L))).thenReturn(List.of());

        assertThatThrownBy(() -> replaceRoleMenusCmdExe.execute(new ReplaceRoleMenusCmd(1L, List.of(10L))))
                .isInstanceOf(BizException.class);
    }
}
