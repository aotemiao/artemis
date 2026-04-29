package com.aotemiao.artemis.workflow.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.app.command.category.CreateFlowCategoryCmdExe;
import com.aotemiao.artemis.workflow.app.command.category.DeleteFlowCategoryCmdExe;
import com.aotemiao.artemis.workflow.app.command.category.UpdateFlowCategoryCmdExe;
import com.aotemiao.artemis.workflow.app.query.category.FindFlowCategoryByIdQryExe;
import com.aotemiao.artemis.workflow.app.query.category.FlowCategoryPageQryExe;
import com.aotemiao.artemis.workflow.app.query.category.ListFlowCategoryQryExe;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FlowCategoryControllerTest {

    private MockMvc mockMvc;
    private CreateFlowCategoryCmdExe createFlowCategoryCmdExe;
    private FindFlowCategoryByIdQryExe findFlowCategoryByIdQryExe;
    private FlowCategoryPageQryExe flowCategoryPageQryExe;
    private ListFlowCategoryQryExe listFlowCategoryQryExe;

    @BeforeEach
    void setUp() {
        createFlowCategoryCmdExe = mock(CreateFlowCategoryCmdExe.class);
        UpdateFlowCategoryCmdExe updateFlowCategoryCmdExe = mock(UpdateFlowCategoryCmdExe.class);
        DeleteFlowCategoryCmdExe deleteFlowCategoryCmdExe = mock(DeleteFlowCategoryCmdExe.class);
        findFlowCategoryByIdQryExe = mock(FindFlowCategoryByIdQryExe.class);
        flowCategoryPageQryExe = mock(FlowCategoryPageQryExe.class);
        listFlowCategoryQryExe = mock(ListFlowCategoryQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new FlowCategoryController(
                        createFlowCategoryCmdExe,
                        updateFlowCategoryCmdExe,
                        deleteFlowCategoryCmdExe,
                        findFlowCategoryByIdQryExe,
                        flowCategoryPageQryExe,
                        listFlowCategoryQryExe))
                .build();
    }

    @Test
    void page_should_return_categories() throws Exception {
        when(flowCategoryPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(category(1L, 0L)), 1));

        mockMvc.perform(get(FlowCategoryController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].categoryName").value("审批流程"));
    }

    @Test
    void getById_should_return_category() throws Exception {
        when(findFlowCategoryByIdQryExe.execute(any())).thenReturn(Optional.of(category(1L, 0L)));

        mockMvc.perform(get(FlowCategoryController.BASE_PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("审批流程"));
    }

    @Test
    void create_should_return_saved_category() throws Exception {
        when(createFlowCategoryCmdExe.execute(any())).thenReturn(category(1L, 0L));

        mockMvc.perform(post(FlowCategoryController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentId\":0,\"categoryName\":\"审批流程\",\"sortOrder\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void tree_should_return_nested_categories() throws Exception {
        when(listFlowCategoryQryExe.execute(any())).thenReturn(List.of(category(1L, 0L), category(2L, 1L)));

        mockMvc.perform(get(FlowCategoryController.BASE_PATH + "/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].children[0].id").value(2));
    }

    private FlowCategory category(Long id, Long parentId) {
        FlowCategory category = new FlowCategory();
        category.setId(id);
        category.setParentId(parentId);
        category.setAncestors(parentId.equals(0L) ? "0" : "0," + parentId);
        category.setCategoryName("审批流程");
        category.setSortOrder(1);
        return category;
    }
}
