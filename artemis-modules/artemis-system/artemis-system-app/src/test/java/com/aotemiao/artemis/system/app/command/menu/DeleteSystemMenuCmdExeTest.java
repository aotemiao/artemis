package com.aotemiao.artemis.system.app.command.menu;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.role.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteSystemMenuCmdExeTest {

    @Mock
    private SystemMenuGateway systemMenuGateway;

    @Mock
    private RoleMenuBindingGateway roleMenuBindingGateway;

    @Mock
    private TenantPackageGateway tenantPackageGateway;

    @InjectMocks
    private DeleteSystemMenuCmdExe deleteSystemMenuCmdExe;

    @Test
    void execute_deletesMenuChildrenAndBindings() {
        SystemMenu root = menu(1L, 0L);
        SystemMenu child = menu(2L, 1L);
        SystemMenu grandChild = menu(3L, 2L);
        SystemMenu sibling = menu(4L, 0L);
        when(systemMenuGateway.findById(1L)).thenReturn(Optional.of(root));
        when(systemMenuGateway.findAll()).thenReturn(List.of(root, child, grandChild, sibling));

        deleteSystemMenuCmdExe.execute(new DeleteSystemMenuCmd(1L));

        verify(roleMenuBindingGateway).deleteByMenuIds(List.of(1L, 2L, 3L));
        verify(tenantPackageGateway).deleteMenuBindingsByMenuIds(List.of(1L, 2L, 3L));
        verify(systemMenuGateway).deleteByIds(List.of(1L, 2L, 3L));
    }

    private static SystemMenu menu(Long id, Long parentId) {
        SystemMenu menu = new SystemMenu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuType(SystemMenu.TYPE_MENU);
        menu.setMenuName("菜单" + id);
        menu.setEnabled(true);
        menu.setVisible(true);
        return menu;
    }
}
