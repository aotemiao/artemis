package com.aotemiao.artemis.system.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.system.app.command.department.CreateSystemDepartmentCmdExe;
import com.aotemiao.artemis.system.app.command.department.DeleteSystemDepartmentCmdExe;
import com.aotemiao.artemis.system.app.command.department.UpdateSystemDepartmentCmdExe;
import com.aotemiao.artemis.system.app.query.department.FindSystemDepartmentByIdQryExe;
import com.aotemiao.artemis.system.app.query.department.ListSystemDepartmentQryExe;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemDepartmentControllerTest {

    private MockMvc mockMvc;

    private CreateSystemDepartmentCmdExe createSystemDepartmentCmdExe;
    private UpdateSystemDepartmentCmdExe updateSystemDepartmentCmdExe;
    private DeleteSystemDepartmentCmdExe deleteSystemDepartmentCmdExe;
    private FindSystemDepartmentByIdQryExe findSystemDepartmentByIdQryExe;
    private ListSystemDepartmentQryExe listSystemDepartmentQryExe;

    @BeforeEach
    void setUp() {
        createSystemDepartmentCmdExe = mock(CreateSystemDepartmentCmdExe.class);
        updateSystemDepartmentCmdExe = mock(UpdateSystemDepartmentCmdExe.class);
        deleteSystemDepartmentCmdExe = mock(DeleteSystemDepartmentCmdExe.class);
        findSystemDepartmentByIdQryExe = mock(FindSystemDepartmentByIdQryExe.class);
        listSystemDepartmentQryExe = mock(ListSystemDepartmentQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemDepartmentController(
                        createSystemDepartmentCmdExe,
                        updateSystemDepartmentCmdExe,
                        deleteSystemDepartmentCmdExe,
                        findSystemDepartmentByIdQryExe,
                        listSystemDepartmentQryExe))
                .build();
    }

    @Test
    void create_returnsCreatedDepartment() throws Exception {
        when(createSystemDepartmentCmdExe.execute(any())).thenReturn(sampleDepartment(1L, 0L, "0", "总部"));

        mockMvc.perform(post(SystemDepartmentController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId": 0,
                                  "deptName": "总部",
                                  "deptCategory": "COMPANY",
                                  "sortOrder": 0,
                                  "status": "NORMAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptName").value("总部"));
    }

    @Test
    void update_returnsUpdatedDepartment() throws Exception {
        when(updateSystemDepartmentCmdExe.execute(any())).thenReturn(sampleDepartment(2L, 1L, "0,1", "研发中心"));

        mockMvc.perform(put(SystemDepartmentController.BASE_PATH + "/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId": 1,
                                  "deptName": "研发中心",
                                  "deptCategory": "DEPT",
                                  "sortOrder": 10,
                                  "status": "NORMAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptName").value("研发中心"));
    }

    @Test
    void getById_returnsDepartment() throws Exception {
        when(findSystemDepartmentByIdQryExe.execute(any()))
                .thenReturn(Optional.of(sampleDepartment(1L, 0L, "0", "总部")));

        mockMvc.perform(get(SystemDepartmentController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptName").value("总部"));
    }

    @Test
    void tree_returnsNestedDepartments() throws Exception {
        when(listSystemDepartmentQryExe.execute(any()))
                .thenReturn(List.of(sampleDepartment(1L, 0L, "0", "总部"), sampleDepartment(2L, 1L, "0,1", "研发部")));

        mockMvc.perform(get(SystemDepartmentController.BASE_PATH + "/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].children[0].deptName").value("研发部"));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete(SystemDepartmentController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk());
    }

    private static SystemDepartment sampleDepartment(Long id, Long parentId, String ancestors, String deptName) {
        SystemDepartment department = new SystemDepartment();
        department.setId(id);
        department.setParentId(parentId);
        department.setAncestors(ancestors);
        department.setDeptName(deptName);
        department.setDeptCategory("DEPT");
        department.setSortOrder(10);
        department.setStatus("NORMAL");
        return department;
    }
}
