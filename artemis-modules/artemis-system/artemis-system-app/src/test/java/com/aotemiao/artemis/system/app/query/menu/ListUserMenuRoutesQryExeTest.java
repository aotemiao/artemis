package com.aotemiao.artemis.system.app.query.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.role.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.user.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListUserMenuRoutesQryExeTest {

    @Mock
    private UserRoleBindingGateway userRoleBindingGateway;

    @Mock
    private RoleMenuBindingGateway roleMenuBindingGateway;

    @Mock
    private SystemMenuGateway systemMenuGateway;

    @InjectMocks
    private ListUserMenuRoutesQryExe listUserMenuRoutesQryExe;

    @Test
    void execute_returnsVisibleRouteMenusWithAncestors() {
        SystemRole role = new SystemRole();
        role.setId(10L);
        role.setEnabled(true);
        SystemMenu root = menu(1L, 0L, SystemMenu.TYPE_DIRECTORY, true, true);
        SystemMenu route = menu(2L, 1L, SystemMenu.TYPE_MENU, true, true);
        SystemMenu button = menu(3L, 2L, SystemMenu.TYPE_BUTTON, true, true);
        SystemMenu hidden = menu(4L, 1L, SystemMenu.TYPE_MENU, true, false);
        SystemMenu disabled = menu(5L, 1L, SystemMenu.TYPE_MENU, false, true);

        when(userRoleBindingGateway.findRolesByUserId(7L)).thenReturn(List.of(role));
        when(systemMenuGateway.findAll()).thenReturn(List.of(root, route, button, hidden, disabled));
        when(roleMenuBindingGateway.findMenusByRoleIds(List.of(10L)))
                .thenReturn(List.of(route, button, hidden, disabled));

        List<SystemMenu> result = listUserMenuRoutesQryExe.execute(new ListUserMenuRoutesQry(7L));

        assertThat(result).extracting(SystemMenu::getId).containsExactly(1L, 2L);
    }

    private static SystemMenu menu(Long id, Long parentId, String type, boolean enabled, boolean visible) {
        SystemMenu menu = new SystemMenu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuType(type);
        menu.setMenuName("菜单" + id);
        menu.setEnabled(enabled);
        menu.setVisible(visible);
        return menu;
    }
}
