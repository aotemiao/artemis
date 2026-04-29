package com.aotemiao.artemis.workflow.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.app.command.spel.CreateFlowSpelCmdExe;
import com.aotemiao.artemis.workflow.app.command.spel.DeleteFlowSpelCmdExe;
import com.aotemiao.artemis.workflow.app.command.spel.UpdateFlowSpelCmdExe;
import com.aotemiao.artemis.workflow.app.query.spel.FindFlowSpelByIdQryExe;
import com.aotemiao.artemis.workflow.app.query.spel.FlowSpelPageQryExe;
import com.aotemiao.artemis.workflow.app.query.spel.ListFlowSpelQryExe;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FlowSpelControllerTest {

    private MockMvc mockMvc;
    private CreateFlowSpelCmdExe createFlowSpelCmdExe;
    private FindFlowSpelByIdQryExe findFlowSpelByIdQryExe;
    private FlowSpelPageQryExe flowSpelPageQryExe;

    @BeforeEach
    void setUp() {
        createFlowSpelCmdExe = mock(CreateFlowSpelCmdExe.class);
        UpdateFlowSpelCmdExe updateFlowSpelCmdExe = mock(UpdateFlowSpelCmdExe.class);
        DeleteFlowSpelCmdExe deleteFlowSpelCmdExe = mock(DeleteFlowSpelCmdExe.class);
        findFlowSpelByIdQryExe = mock(FindFlowSpelByIdQryExe.class);
        flowSpelPageQryExe = mock(FlowSpelPageQryExe.class);
        ListFlowSpelQryExe listFlowSpelQryExe = mock(ListFlowSpelQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new FlowSpelController(
                        createFlowSpelCmdExe,
                        updateFlowSpelCmdExe,
                        deleteFlowSpelCmdExe,
                        findFlowSpelByIdQryExe,
                        flowSpelPageQryExe,
                        listFlowSpelQryExe))
                .build();
    }

    @Test
    void page_should_return_spels() throws Exception {
        when(flowSpelPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(spel()), 1));

        mockMvc.perform(get(FlowSpelController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].previewExpression").value("#{starter.userId($userId)}"));
    }

    @Test
    void getById_should_return_spel() throws Exception {
        when(findFlowSpelByIdQryExe.execute(any())).thenReturn(Optional.of(spel()));

        mockMvc.perform(get(FlowSpelController.BASE_PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.componentName").value("starter"));
    }

    @Test
    void create_should_return_saved_spel() throws Exception {
        when(createFlowSpelCmdExe.execute(any())).thenReturn(spel());

        mockMvc.perform(post(FlowSpelController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"componentName\":\"starter\",\"methodName\":\"userId\","
                                + "\"previewExpression\":\"#{starter.userId($userId)}\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    private FlowSpel spel() {
        FlowSpel spel = new FlowSpel();
        spel.setId(1L);
        spel.setComponentName("starter");
        spel.setMethodName("userId");
        spel.setParameters("$userId");
        spel.setPreviewExpression("#{starter.userId($userId)}");
        spel.setStatus(1);
        return spel;
    }
}
