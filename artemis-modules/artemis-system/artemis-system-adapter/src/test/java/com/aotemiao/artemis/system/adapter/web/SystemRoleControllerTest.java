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
import com.aotemiao.artemis.system.app.command.CreateSystemRoleCmdExe;
import com.aotemiao.artemis.system.app.command.UpdateSystemRoleCmdExe;
import com.aotemiao.artemis.system.app.query.FindSystemRoleByIdQryExe;
import com.aotemiao.artemis.system.app.query.SystemRolePageQryExe;
import com.aotemiao.artemis.system.domain.model.SystemRole;
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

    @BeforeEach
    void setUp() {
        createSystemRoleCmdExe = mock(CreateSystemRoleCmdExe.class);
        updateSystemRoleCmdExe = mock(UpdateSystemRoleCmdExe.class);
        findSystemRoleByIdQryExe = mock(FindSystemRoleByIdQryExe.class);
        systemRolePageQryExe = mock(SystemRolePageQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemRoleController(
                        createSystemRoleCmdExe, updateSystemRoleCmdExe, findSystemRoleByIdQryExe, systemRolePageQryExe))
                .build();
    }

    @Test
    void create_returnsCreatedRole() throws Exception {
        SystemRole systemRole = new SystemRole();
        systemRole.setId(1L);
        systemRole.setRoleKey("super-admin");
        systemRole.setRoleName("超级管理员");
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
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void update_returnsUpdatedRole() throws Exception {
        SystemRole systemRole = new SystemRole();
        systemRole.setId(1L);
        systemRole.setRoleKey("system-admin");
        systemRole.setRoleName("系统管理员");
        systemRole.setEnabled(false);
        when(updateSystemRoleCmdExe.execute(any())).thenReturn(systemRole);

        mockMvc.perform(put(SystemRoleController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleKey": "system-admin",
                                  "roleName": "系统管理员",
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleKey").value("system-admin"))
                .andExpect(jsonPath("$.data.roleName").value("系统管理员"))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void getById_returnsRole() throws Exception {
        SystemRole systemRole = new SystemRole();
        systemRole.setId(1L);
        systemRole.setRoleKey("super-admin");
        systemRole.setRoleName("超级管理员");
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
        systemRole.setEnabled(true);
        when(systemRolePageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(systemRole), 1));

        mockMvc.perform(get(SystemRoleController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].roleKey").value("super-admin"));
    }
}
