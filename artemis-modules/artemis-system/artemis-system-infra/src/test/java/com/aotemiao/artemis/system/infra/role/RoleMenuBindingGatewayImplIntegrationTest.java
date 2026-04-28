package com.aotemiao.artemis.system.infra.role;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.role.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.role.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.menu.SystemMenuGatewayImpl.class,
    com.aotemiao.artemis.system.infra.gateway.role.SystemRoleGatewayImpl.class,
    com.aotemiao.artemis.system.infra.gateway.role.RoleMenuBindingGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:role_menu_binding_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class RoleMenuBindingGatewayImplIntegrationTest {

    @Autowired
    private SystemRoleGateway systemRoleGateway;

    @Autowired
    private SystemMenuGateway systemMenuGateway;

    @Autowired
    private RoleMenuBindingGateway roleMenuBindingGateway;

    @Test
    void replaceMenus_thenFindMenusByRoleId_returnsCurrentBindings() {
        SystemRole role = saveRole("super-admin", "超级管理员");
        SystemMenu userMenu = saveMenu("用户管理", "system:user:list", 10);
        SystemMenu roleMenu = saveMenu("角色管理", "system:role:list", 20);

        roleMenuBindingGateway.replaceMenus(
                role.getId(), List.of(userMenu.getId(), roleMenu.getId(), roleMenu.getId()));

        List<SystemMenu> firstBinding = roleMenuBindingGateway.findMenusByRoleId(role.getId());
        assertThat(firstBinding).hasSize(2);
        assertThat(firstBinding.stream().map(SystemMenu::getPermissionCode))
                .containsExactlyInAnyOrder("system:user:list", "system:role:list");

        roleMenuBindingGateway.replaceMenus(role.getId(), List.of(userMenu.getId()));

        List<SystemMenu> secondBinding = roleMenuBindingGateway.findMenusByRoleId(role.getId());
        assertThat(secondBinding).hasSize(1);
        assertThat(secondBinding.getFirst().getPermissionCode()).isEqualTo("system:user:list");
    }

    @Test
    void findMenusByRoleIds_returnsDistinctMenusAcrossRoles() {
        SystemRole admin = saveRole("super-admin", "超级管理员");
        SystemRole auditor = saveRole("auditor", "审计员");
        SystemMenu userMenu = saveMenu("用户管理", "system:user:list", 10);
        SystemMenu roleMenu = saveMenu("角色管理", "system:role:list", 20);

        roleMenuBindingGateway.replaceMenus(admin.getId(), List.of(userMenu.getId(), roleMenu.getId()));
        roleMenuBindingGateway.replaceMenus(auditor.getId(), List.of(roleMenu.getId()));

        List<SystemMenu> menus = roleMenuBindingGateway.findMenusByRoleIds(List.of(admin.getId(), auditor.getId()));

        assertThat(menus.stream().map(SystemMenu::getPermissionCode))
                .containsExactlyInAnyOrder("system:user:list", "system:role:list");
    }

    private SystemRole saveRole(String roleKey, String roleName) {
        SystemRole role = new SystemRole();
        role.setRoleKey(roleKey);
        role.setRoleName(roleName);
        role.setEnabled(true);
        return systemRoleGateway.save(role);
    }

    private SystemMenu saveMenu(String menuName, String permissionCode, int sortOrder) {
        SystemMenu menu = new SystemMenu();
        menu.setParentId(0L);
        menu.setMenuType(SystemMenu.TYPE_MENU);
        menu.setMenuName(menuName);
        menu.setSortOrder(sortOrder);
        menu.setPath("/" + permissionCode.replace(':', '/'));
        menu.setPermissionCode(permissionCode);
        menu.setVisible(true);
        menu.setEnabled(true);
        return systemMenuGateway.save(menu);
    }
}
