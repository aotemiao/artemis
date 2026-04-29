package com.aotemiao.artemis.system.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.menu.CreateSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.command.menu.DeleteSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.command.menu.UpdateSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.query.menu.FindSystemMenuByIdQryExe;
import com.aotemiao.artemis.system.app.query.menu.ListRoleMenusQryExe;
import com.aotemiao.artemis.system.app.query.menu.ListSystemMenusQryExe;
import com.aotemiao.artemis.system.app.query.menu.ListUserMenuRoutesQryExe;
import com.aotemiao.artemis.system.app.query.tenant.FindTenantPackageByIdQryExe;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import jakarta.servlet.ServletException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemMenuControllerTest {

    private MockMvc mockMvc;

    private CreateSystemMenuCmdExe createSystemMenuCmdExe;
    private UpdateSystemMenuCmdExe updateSystemMenuCmdExe;
    private DeleteSystemMenuCmdExe deleteSystemMenuCmdExe;
    private FindSystemMenuByIdQryExe findSystemMenuByIdQryExe;
    private ListSystemMenusQryExe listSystemMenusQryExe;
    private ListRoleMenusQryExe listRoleMenusQryExe;
    private ListUserMenuRoutesQryExe listUserMenuRoutesQryExe;
    private FindTenantPackageByIdQryExe findTenantPackageByIdQryExe;

    @BeforeEach
    void setUp() {
        createSystemMenuCmdExe = mock(CreateSystemMenuCmdExe.class);
        updateSystemMenuCmdExe = mock(UpdateSystemMenuCmdExe.class);
        deleteSystemMenuCmdExe = mock(DeleteSystemMenuCmdExe.class);
        findSystemMenuByIdQryExe = mock(FindSystemMenuByIdQryExe.class);
        listSystemMenusQryExe = mock(ListSystemMenusQryExe.class);
        listRoleMenusQryExe = mock(ListRoleMenusQryExe.class);
        listUserMenuRoutesQryExe = mock(ListUserMenuRoutesQryExe.class);
        findTenantPackageByIdQryExe = mock(FindTenantPackageByIdQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemMenuController(
                        createSystemMenuCmdExe,
                        updateSystemMenuCmdExe,
                        deleteSystemMenuCmdExe,
                        findSystemMenuByIdQryExe,
                        listSystemMenusQryExe,
                        listRoleMenusQryExe,
                        listUserMenuRoutesQryExe,
                        findTenantPackageByIdQryExe))
                .build();
    }

    @Test
    void create_returnsCreatedMenu() throws Exception {
        SystemMenu menu = sampleMenu();
        when(createSystemMenuCmdExe.execute(any())).thenReturn(menu);

        mockMvc.perform(post(SystemMenuController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId": 0,
                                  "menuType": "MENU",
                                  "menuName": "用户管理",
                                  "sortOrder": 10,
                                  "path": "/system/users",
                                  "component": "system/user/index",
                                  "queryParam": "tab=profile",
                                  "externalLink": false,
                                  "cacheable": true,
                                  "permissionCode": "system:user:list",
                                  "icon": "user",
                                  "visible": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.menuName").value("用户管理"))
                .andExpect(jsonPath("$.data.icon").value("user"))
                .andExpect(jsonPath("$.data.permissionCode").value("system:user:list"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void update_returnsUpdatedMenu() throws Exception {
        SystemMenu menu = sampleMenu();
        menu.setMenuName("平台用户");
        menu.setEnabled(false);
        when(updateSystemMenuCmdExe.execute(any())).thenReturn(menu);

        mockMvc.perform(put(SystemMenuController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId": 0,
                                  "menuType": "MENU",
                                  "menuName": "平台用户",
                                  "sortOrder": 10,
                                  "path": "/system/users",
                                  "component": "system/user/index",
                                  "queryParam": "tab=profile",
                                  "externalLink": false,
                                  "cacheable": true,
                                  "permissionCode": "system:user:list",
                                  "icon": "user",
                                  "visible": true,
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuName").value("平台用户"))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void getById_returnsMenu() throws Exception {
        when(findSystemMenuByIdQryExe.execute(any())).thenReturn(Optional.of(sampleMenu()));

        mockMvc.perform(get(SystemMenuController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuName").value("用户管理"))
                .andExpect(jsonPath("$.data.permissionCode").value("system:user:list"));
    }

    @Test
    void getById_whenMissing_throwsBizException() {
        when(findSystemMenuByIdQryExe.execute(any())).thenReturn(Optional.empty());

        ServletException exception = assertThrows(
                ServletException.class, () -> mockMvc.perform(get(SystemMenuController.BASE_PATH + "/{id}", 99L)));

        assertThat(exception.getCause()).isInstanceOf(BizException.class);
    }

    @Test
    void list_returnsMenus() throws Exception {
        when(listSystemMenusQryExe.execute(any())).thenReturn(List.of(sampleMenu()));

        mockMvc.perform(get(SystemMenuController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].menuName").value("用户管理"));
    }

    @Test
    void tree_returnsNestedMenus() throws Exception {
        SystemMenu child = sampleMenu();
        child.setParentId(2L);
        SystemMenu root = sampleMenu();
        root.setId(2L);
        root.setParentId(0L);
        root.setMenuType(SystemMenu.TYPE_DIRECTORY);
        root.setMenuName("系统管理");
        when(listSystemMenusQryExe.execute(any())).thenReturn(List.of(root, child));

        mockMvc.perform(get(SystemMenuController.BASE_PATH + "/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].menu.menuName").value("系统管理"))
                .andExpect(jsonPath("$.data[0].children[0].menu.menuName").value("用户管理"));
    }

    @Test
    void userRoutes_returnsRouteTree() throws Exception {
        SystemMenu child = sampleMenu();
        child.setParentId(2L);
        SystemMenu root = sampleMenu();
        root.setId(2L);
        root.setParentId(0L);
        root.setMenuType(SystemMenu.TYPE_DIRECTORY);
        root.setMenuName("系统管理");
        root.setPath("/system");
        when(listUserMenuRoutesQryExe.execute(any())).thenReturn(List.of(root, child));

        mockMvc.perform(get(SystemMenuController.BASE_PATH + "/routes/users/{userId}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("系统管理"))
                .andExpect(jsonPath("$.data[0].children[0].name").value("用户管理"));
    }

    @Test
    void delete_returnsTrue() throws Exception {
        mockMvc.perform(delete(SystemMenuController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    private SystemMenu sampleMenu() {
        SystemMenu menu = new SystemMenu();
        menu.setId(1L);
        menu.setParentId(0L);
        menu.setMenuType(SystemMenu.TYPE_MENU);
        menu.setMenuName("用户管理");
        menu.setSortOrder(10);
        menu.setPath("/system/users");
        menu.setComponent("system/user/index");
        menu.setQueryParam("tab=profile");
        menu.setExternalLink(false);
        menu.setCacheable(true);
        menu.setPermissionCode("system:user:list");
        menu.setIcon("user");
        menu.setVisible(true);
        menu.setEnabled(true);
        menu.setRemarks("用户菜单");
        return menu;
    }
}
