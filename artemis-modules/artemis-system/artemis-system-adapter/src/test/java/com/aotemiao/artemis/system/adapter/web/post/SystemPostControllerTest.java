package com.aotemiao.artemis.system.adapter.web.post;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.app.command.post.CreateSystemPostCmdExe;
import com.aotemiao.artemis.system.app.command.post.DeleteSystemPostCmdExe;
import com.aotemiao.artemis.system.app.command.post.UpdateSystemPostCmdExe;
import com.aotemiao.artemis.system.app.query.department.ListSystemDepartmentQryExe;
import com.aotemiao.artemis.system.app.query.post.FindSystemPostByIdQryExe;
import com.aotemiao.artemis.system.app.query.post.ListSystemPostQryExe;
import com.aotemiao.artemis.system.app.query.post.SystemPostPageQryExe;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemPostControllerTest {

    private MockMvc mockMvc;

    private CreateSystemPostCmdExe createSystemPostCmdExe;
    private UpdateSystemPostCmdExe updateSystemPostCmdExe;
    private DeleteSystemPostCmdExe deleteSystemPostCmdExe;
    private FindSystemPostByIdQryExe findSystemPostByIdQryExe;
    private SystemPostPageQryExe systemPostPageQryExe;
    private ListSystemPostQryExe listSystemPostQryExe;
    private ListSystemDepartmentQryExe listSystemDepartmentQryExe;

    @BeforeEach
    void setUp() {
        createSystemPostCmdExe = mock(CreateSystemPostCmdExe.class);
        updateSystemPostCmdExe = mock(UpdateSystemPostCmdExe.class);
        deleteSystemPostCmdExe = mock(DeleteSystemPostCmdExe.class);
        findSystemPostByIdQryExe = mock(FindSystemPostByIdQryExe.class);
        systemPostPageQryExe = mock(SystemPostPageQryExe.class);
        listSystemPostQryExe = mock(ListSystemPostQryExe.class);
        listSystemDepartmentQryExe = mock(ListSystemDepartmentQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemPostController(
                        createSystemPostCmdExe,
                        updateSystemPostCmdExe,
                        deleteSystemPostCmdExe,
                        findSystemPostByIdQryExe,
                        systemPostPageQryExe,
                        listSystemPostQryExe,
                        listSystemDepartmentQryExe))
                .build();
    }

    @Test
    void create_returnsCreatedPost() throws Exception {
        when(createSystemPostCmdExe.execute(any())).thenReturn(samplePost());

        mockMvc.perform(post(SystemPostController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deptId": 1,
                                  "postCode": "dev",
                                  "postCategory": "TECH",
                                  "postName": "开发工程师",
                                  "sortOrder": 10,
                                  "status": "NORMAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postCode").value("dev"));
    }

    @Test
    void update_returnsUpdatedPost() throws Exception {
        SystemPost post = samplePost();
        post.setPostName("研发工程师");
        when(updateSystemPostCmdExe.execute(any())).thenReturn(post);

        mockMvc.perform(put(SystemPostController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deptId": 1,
                                  "postCode": "dev",
                                  "postCategory": "TECH",
                                  "postName": "研发工程师",
                                  "sortOrder": 10,
                                  "status": "NORMAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postName").value("研发工程师"));
    }

    @Test
    void getById_returnsPost() throws Exception {
        when(findSystemPostByIdQryExe.execute(any())).thenReturn(Optional.of(samplePost()));

        mockMvc.perform(get(SystemPostController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postCode").value("dev"));
    }

    @Test
    void page_returnsPostPage() throws Exception {
        when(systemPostPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(samplePost()), 1));

        mockMvc.perform(get(SystemPostController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].postCode").value("dev"));
    }

    @Test
    void select_returnsPosts() throws Exception {
        when(listSystemPostQryExe.execute(any())).thenReturn(List.of(samplePost()));

        mockMvc.perform(get(SystemPostController.BASE_PATH + "/select"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].postCode").value("dev"));
    }

    @Test
    void departmentTree_returnsNestedDepartments() throws Exception {
        when(listSystemDepartmentQryExe.execute(any()))
                .thenReturn(List.of(sampleDepartment(1L, 0L, "0", "总部"), sampleDepartment(2L, 1L, "0,1", "研发部")));

        mockMvc.perform(get(SystemPostController.BASE_PATH + "/departments/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].children[0].deptName").value("研发部"));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete(SystemPostController.BASE_PATH + "/{id}", 1L)).andExpect(status().isOk());
    }

    private static SystemPost samplePost() {
        SystemPost post = new SystemPost();
        post.setId(1L);
        post.setDeptId(1L);
        post.setPostCode("dev");
        post.setPostCategory("TECH");
        post.setPostName("开发工程师");
        post.setSortOrder(10);
        post.setStatus("NORMAL");
        return post;
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
