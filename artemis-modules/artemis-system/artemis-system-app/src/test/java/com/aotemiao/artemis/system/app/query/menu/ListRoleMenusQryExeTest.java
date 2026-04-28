package com.aotemiao.artemis.system.app.query.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListRoleMenusQryExeTest {

    @Mock
    private RoleMenuBindingGateway roleMenuBindingGateway;

    @InjectMocks
    private ListRoleMenusQryExe listRoleMenusQryExe;

    @Test
    void execute_returnsGatewayResult() {
        SystemMenu menu = new SystemMenu();
        menu.setId(1L);
        when(roleMenuBindingGateway.findMenusByRoleId(7L)).thenReturn(List.of(menu));

        List<SystemMenu> result = listRoleMenusQryExe.execute(new ListRoleMenusQry(7L));

        assertThat(result).containsExactly(menu);
        verify(roleMenuBindingGateway).findMenusByRoleId(7L);
    }
}
