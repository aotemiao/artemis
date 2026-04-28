package com.aotemiao.artemis.system.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.system.domain.gateway.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.SystemMenuGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:system_menu_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class SystemMenuGatewayImplIntegrationTest {

    @Autowired
    private SystemMenuGateway systemMenuGateway;

    @Test
    void save_thenFindById_returnsSameData() {
        SystemMenu menu = new SystemMenu();
        menu.setParentId(0L);
        menu.setMenuType(SystemMenu.TYPE_MENU);
        menu.setMenuName("用户管理");
        menu.setSortOrder(10);
        menu.setPath("/system/users");
        menu.setComponent("system/user/index");
        menu.setPermissionCode("system:user:list");
        menu.setVisible(true);
        menu.setEnabled(true);

        SystemMenu saved = systemMenuGateway.save(menu);

        assertThat(saved.getId()).isNotNull();
        Optional<SystemMenu> found = systemMenuGateway.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getMenuName()).isEqualTo("用户管理");
        assertThat(found.get().getPermissionCode()).isEqualTo("system:user:list");
    }

    @Test
    void findAll_returnsMenusOrderedByParentAndSortOrder() {
        SystemMenu root = new SystemMenu();
        root.setParentId(0L);
        root.setMenuType(SystemMenu.TYPE_DIRECTORY);
        root.setMenuName("系统管理");
        root.setSortOrder(10);
        root.setVisible(true);
        root.setEnabled(true);
        root = systemMenuGateway.save(root);

        SystemMenu roleMenu = new SystemMenu();
        roleMenu.setParentId(root.getId());
        roleMenu.setMenuType(SystemMenu.TYPE_MENU);
        roleMenu.setMenuName("角色管理");
        roleMenu.setSortOrder(20);
        roleMenu.setPath("/system/roles");
        roleMenu.setPermissionCode("system:role:list");
        roleMenu.setVisible(true);
        roleMenu.setEnabled(true);
        systemMenuGateway.save(roleMenu);

        SystemMenu userMenu = new SystemMenu();
        userMenu.setParentId(root.getId());
        userMenu.setMenuType(SystemMenu.TYPE_MENU);
        userMenu.setMenuName("用户管理");
        userMenu.setSortOrder(10);
        userMenu.setPath("/system/users");
        userMenu.setPermissionCode("system:user:list");
        userMenu.setVisible(true);
        userMenu.setEnabled(true);
        systemMenuGateway.save(userMenu);

        List<SystemMenu> menus = systemMenuGateway.findAll();

        assertThat(menus.stream().map(SystemMenu::getMenuName)).containsExactly("系统管理", "用户管理", "角色管理");
    }
}
