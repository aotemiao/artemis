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

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.CreateSystemUserCmdExe;
import com.aotemiao.artemis.system.app.command.ReplaceUserRolesCmdExe;
import com.aotemiao.artemis.system.app.command.UpdateSystemUserCmdExe;
import com.aotemiao.artemis.system.app.query.FindSystemUserByIdQryExe;
import com.aotemiao.artemis.system.app.query.ListUserRolesQryExe;
import com.aotemiao.artemis.system.app.query.SystemUserPageQryExe;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import jakarta.servlet.ServletException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemUserControllerTest {

    private MockMvc mockMvc;

    private CreateSystemUserCmdExe createSystemUserCmdExe;
    private UpdateSystemUserCmdExe updateSystemUserCmdExe;
    private FindSystemUserByIdQryExe findSystemUserByIdQryExe;
    private SystemUserPageQryExe systemUserPageQryExe;
    private ListUserRolesQryExe listUserRolesQryExe;
    private ReplaceUserRolesCmdExe replaceUserRolesCmdExe;

    @BeforeEach
    void setUp() {
        createSystemUserCmdExe = mock(CreateSystemUserCmdExe.class);
        updateSystemUserCmdExe = mock(UpdateSystemUserCmdExe.class);
        findSystemUserByIdQryExe = mock(FindSystemUserByIdQryExe.class);
        systemUserPageQryExe = mock(SystemUserPageQryExe.class);
        listUserRolesQryExe = mock(ListUserRolesQryExe.class);
        replaceUserRolesCmdExe = mock(ReplaceUserRolesCmdExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(
                        createSystemUserCmdExe,
                        updateSystemUserCmdExe,
                        findSystemUserByIdQryExe,
                        systemUserPageQryExe,
                        listUserRolesQryExe,
                        replaceUserRolesCmdExe))
                .build();
    }

    @Test
    void create_returnsCreatedUser() throws Exception {
        SystemUser systemUser = new SystemUser();
        systemUser.setId(1L);
        systemUser.setUsername("admin");
        systemUser.setDisplayName("管理员");
        systemUser.setEnabled(true);
        when(createSystemUserCmdExe.execute(any())).thenReturn(systemUser);

        mockMvc.perform(post(SystemUserController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "displayName": "管理员",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.displayName").value("管理员"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void update_returnsUpdatedUser() throws Exception {
        SystemUser systemUser = new SystemUser();
        systemUser.setId(1L);
        systemUser.setUsername("admin");
        systemUser.setDisplayName("平台管理员");
        systemUser.setEnabled(false);
        when(updateSystemUserCmdExe.execute(any())).thenReturn(systemUser);

        mockMvc.perform(put(SystemUserController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "平台管理员",
                                  "password": "654321",
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("平台管理员"))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void getById_returnsUser() throws Exception {
        SystemUser systemUser = new SystemUser();
        systemUser.setId(1L);
        systemUser.setUsername("admin");
        systemUser.setDisplayName("管理员");
        systemUser.setEnabled(true);
        when(findSystemUserByIdQryExe.execute(any())).thenReturn(Optional.of(systemUser));

        mockMvc.perform(get(SystemUserController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.displayName").value("管理员"));
    }

    @Test
    void getById_whenMissing_throwsBizException() {
        when(findSystemUserByIdQryExe.execute(any())).thenReturn(Optional.empty());

        ServletException exception = assertThrows(
                ServletException.class, () -> mockMvc.perform(get(SystemUserController.BASE_PATH + "/{id}", 99L)));

        assertThat(exception.getCause()).isInstanceOf(BizException.class);
    }

    @Test
    void page_returnsUserPage() throws Exception {
        SystemUser systemUser = new SystemUser();
        systemUser.setId(1L);
        systemUser.setUsername("admin");
        systemUser.setDisplayName("管理员");
        systemUser.setEnabled(true);
        when(systemUserPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(systemUser), 1));

        mockMvc.perform(get(SystemUserController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("admin"));
    }

    @Test
    void listRoles_returnsRoleList() throws Exception {
        SystemRole role = new SystemRole();
        role.setId(10L);
        role.setRoleKey("super-admin");
        role.setRoleName("超级管理员");
        role.setEnabled(true);
        when(listUserRolesQryExe.execute(any())).thenReturn(List.of(role));

        mockMvc.perform(get(SystemUserController.BASE_PATH + "/{userId}/roles", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roleKey").value("super-admin"))
                .andExpect(jsonPath("$.data[0].roleName").value("超级管理员"));
    }

    @Test
    void replaceRoles_returnsUpdatedRoleList() throws Exception {
        SystemRole role = new SystemRole();
        role.setId(10L);
        role.setRoleKey("super-admin");
        role.setRoleName("超级管理员");
        role.setEnabled(true);
        when(replaceUserRolesCmdExe.execute(any())).thenReturn(List.of(role));

        mockMvc.perform(put(SystemUserController.BASE_PATH + "/{userId}/roles", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleIds": [10]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roleKey").value("super-admin"));
    }
}
