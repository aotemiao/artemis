package com.aotemiao.artemis.system.adapter.web;

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

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.menu.CreateSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.command.menu.UpdateSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.query.menu.FindSystemMenuByIdQryExe;
import com.aotemiao.artemis.system.app.query.menu.ListSystemMenusQryExe;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
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
    private FindSystemMenuByIdQryExe findSystemMenuByIdQryExe;
    private ListSystemMenusQryExe listSystemMenusQryExe;

    @BeforeEach
    void setUp() {
        createSystemMenuCmdExe = mock(CreateSystemMenuCmdExe.class);
        updateSystemMenuCmdExe = mock(UpdateSystemMenuCmdExe.class);
        findSystemMenuByIdQryExe = mock(FindSystemMenuByIdQryExe.class);
        listSystemMenusQryExe = mock(ListSystemMenusQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemMenuController(
                        createSystemMenuCmdExe,
                        updateSystemMenuCmdExe,
                        findSystemMenuByIdQryExe,
                        listSystemMenusQryExe))
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
                                  "permissionCode": "system:user:list",
                                  "visible": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.menuName").value("用户管理"))
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
                                  "permissionCode": "system:user:list",
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

    private SystemMenu sampleMenu() {
        SystemMenu menu = new SystemMenu();
        menu.setId(1L);
        menu.setParentId(0L);
        menu.setMenuType(SystemMenu.TYPE_MENU);
        menu.setMenuName("用户管理");
        menu.setSortOrder(10);
        menu.setPath("/system/users");
        menu.setComponent("system/user/index");
        menu.setPermissionCode("system:user:list");
        menu.setVisible(true);
        menu.setEnabled(true);
        return menu;
    }
}
