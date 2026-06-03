package com.aotemiao.artemis.system.adapter.web.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.role.CreateSystemRoleCmdExe;
import com.aotemiao.artemis.system.app.command.role.ReplaceRoleDataScopeCmdExe;
import com.aotemiao.artemis.system.app.command.role.ReplaceRoleMenusCmdExe;
import com.aotemiao.artemis.system.app.command.role.UpdateSystemRoleCmdExe;
import com.aotemiao.artemis.system.app.query.menu.ListRoleMenusQryExe;
import com.aotemiao.artemis.system.app.query.role.FindSystemRoleByIdQryExe;
import com.aotemiao.artemis.system.app.query.role.ListRoleDepartmentsQryExe;
import com.aotemiao.artemis.system.app.query.role.SystemRolePageQryExe;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import jakarta.servlet.ServletException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemRoleControllerTest {

    private MockMvc mockMvc;

    private CreateSystemRoleCmdExe createSystemRoleCmdExe;
    private UpdateSystemRoleCmdExe updateSystemRoleCmdExe;
    private FindSystemRoleByIdQryExe findSystemRoleByIdQryExe;
    private SystemRolePageQryExe systemRolePageQryExe;
    private ListRoleMenusQryExe listRoleMenusQryExe;
    private ReplaceRoleMenusCmdExe replaceRoleMenusCmdExe;
    private ListRoleDepartmentsQryExe listRoleDepartmentsQryExe;
    private ReplaceRoleDataScopeCmdExe replaceRoleDataScopeCmdExe;

    @BeforeEach
    void setUp() {
        createSystemRoleCmdExe = mock(CreateSystemRoleCmdExe.class);
        updateSystemRoleCmdExe = mock(UpdateSystemRoleCmdExe.class);
        findSystemRoleByIdQryExe = mock(FindSystemRoleByIdQryExe.class);
        systemRolePageQryExe = mock(SystemRolePageQryExe.class);
        listRoleMenusQryExe = mock(ListRoleMenusQryExe.class);
        replaceRoleMenusCmdExe = mock(ReplaceRoleMenusCmdExe.class);
        listRoleDepartmentsQryExe = mock(ListRoleDepartmentsQryExe.class);
        replaceRoleDataScopeCmdExe = mock(ReplaceRoleDataScopeCmdExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemRoleController(
                        createSystemRoleCmdExe,
                        updateSystemRoleCmdExe,
                        findSystemRoleByIdQryExe,
                        systemRolePageQryExe,
                        listRoleMenusQryExe,
                        replaceRoleMenusCmdExe,
                        listRoleDepartmentsQryExe,
                        replaceRoleDataScopeCmdExe))
                .build();
    }

    @Test
    void create_returnsCreatedRole() throws Exception {
        SystemRole systemRole = new SystemRole();
        systemRole.setId(1L);
        systemRole.setRoleKey("super-admin");
        systemRole.setRoleName("超级管理员");
        systemRole.setDataScope("ALL");
        systemRole.setEnabled(true);
        when(createSystemRoleCmdExe.execute(any())).thenReturn(systemRole);

        mockMvc.perform(post(SystemRoleController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleKey": "super-admin",
                                  "roleName": "超级管理员"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.roleKey").value("super-admin"))
                .andExpect(jsonPath("$.data.roleName").value("超级管理员"))
                .andExpect(jsonPath("$.data.dataScope").value("ALL"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void update_returnsUpdatedRole() throws Exception {
        SystemRole systemRole = new SystemRole();
        systemRole.setId(1L);
        systemRole.setRoleKey("system-admin");
        systemRole.setRoleName("系统管理员");
        systemRole.setDataScope("CUSTOM");
        systemRole.setEnabled(false);
        when(updateSystemRoleCmdExe.execute(any())).thenReturn(systemRole);

        mockMvc.perform(put(SystemRoleController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleKey": "system-admin",
                                  "roleName": "系统管理员",
                                  "dataScope": "CUSTOM",
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleKey").value("system-admin"))
                .andExpect(jsonPath("$.data.roleName").value("系统管理员"))
                .andExpect(jsonPath("$.data.dataScope").value("CUSTOM"))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void getById_returnsRole() throws Exception {
        SystemRole systemRole = new SystemRole();
        systemRole.setId(1L);
        systemRole.setRoleKey("super-admin");
        systemRole.setRoleName("超级管理员");
        systemRole.setDataScope("ALL");
        systemRole.setEnabled(true);
        when(findSystemRoleByIdQryExe.execute(any())).thenReturn(Optional.of(systemRole));

        mockMvc.perform(get(SystemRoleController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleKey").value("super-admin"))
                .andExpect(jsonPath("$.data.roleName").value("超级管理员"));
    }

    @Test
    void getById_whenMissing_throwsBizException() {
        when(findSystemRoleByIdQryExe.execute(any())).thenReturn(Optional.empty());

        ServletException exception = assertThrows(
                ServletException.class, () -> mockMvc.perform(get(SystemRoleController.BASE_PATH + "/{id}", 99L)));

        assertThat(exception.getCause()).isInstanceOf(BizException.class);
    }

    @Test
    void page_returnsRolePage() throws Exception {
        SystemRole systemRole = new SystemRole();
        systemRole.setId(1L);
        systemRole.setRoleKey("super-admin");
        systemRole.setRoleName("超级管理员");
        systemRole.setDataScope("ALL");
        systemRole.setEnabled(true);
        when(systemRolePageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(systemRole), 1));

        mockMvc.perform(get(SystemRoleController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].roleKey").value("super-admin"));
    }

    @Test
    void listMenus_returnsRoleMenus() throws Exception {
        SystemMenu menu = sampleMenu();
        when(listRoleMenusQryExe.execute(any())).thenReturn(List.of(menu));

        mockMvc.perform(get(SystemRoleController.BASE_PATH + "/{roleId}/menus", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].menuName").value("用户管理"))
                .andExpect(jsonPath("$.data[0].permissionCode").value("system:user:list"));
    }

    @Test
    void replaceMenus_returnsUpdatedRoleMenus() throws Exception {
        SystemMenu menu = sampleMenu();
        when(replaceRoleMenusCmdExe.execute(any())).thenReturn(List.of(menu));

        mockMvc.perform(put(SystemRoleController.BASE_PATH + "/{roleId}/menus", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "menuIds": [10]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].permissionCode").value("system:user:list"));
    }

    @Test
    void listDepartments_returnsRoleDepartmentIds() throws Exception {
        when(listRoleDepartmentsQryExe.execute(any())).thenReturn(List.of(1L, 2L));

        mockMvc.perform(get(SystemRoleController.BASE_PATH + "/{roleId}/departments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(1))
                .andExpect(jsonPath("$.data[1]").value(2));
    }

    @Test
    void replaceDataScope_returnsUpdatedDepartmentIds() throws Exception {
        when(replaceRoleDataScopeCmdExe.execute(any())).thenReturn(List.of(1L, 2L));

        mockMvc.perform(put(SystemRoleController.BASE_PATH + "/{roleId}/data-scope", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dataScope": "CUSTOM",
                                  "departmentIds": [1, 2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(1))
                .andExpect(jsonPath("$.data[1]").value(2));
    }

    private SystemMenu sampleMenu() {
        SystemMenu menu = new SystemMenu();
        menu.setId(10L);
        menu.setParentId(0L);
        menu.setMenuType(SystemMenu.TYPE_MENU);
        menu.setMenuName("用户管理");
        menu.setSortOrder(10);
        menu.setPath("/system/users");
        menu.setPermissionCode("system:user:list");
        menu.setVisible(true);
        menu.setEnabled(true);
        return menu;
    }
}
