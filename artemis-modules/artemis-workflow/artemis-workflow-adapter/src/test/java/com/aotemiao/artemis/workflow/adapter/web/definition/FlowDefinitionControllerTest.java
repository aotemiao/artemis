package com.aotemiao.artemis.workflow.adapter.web.definition;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.app.command.definition.ActivateFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.CancelPublishFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.CopyFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.CreateFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.DeleteFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.PublishFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.SuspendFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.SyncFlowDefinitionTenantCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.UpdateFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.query.definition.FindFlowDefinitionByIdQryExe;
import com.aotemiao.artemis.workflow.app.query.definition.FlowDefinitionPageQryExe;
import com.aotemiao.artemis.workflow.app.query.definition.ListFlowDefinitionQryExe;
import com.aotemiao.artemis.workflow.app.query.definition.ListUnpublishedFlowDefinitionQryExe;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FlowDefinitionControllerTest {

    private MockMvc mockMvc;
    private CreateFlowDefinitionCmdExe createFlowDefinitionCmdExe;
    private PublishFlowDefinitionCmdExe publishFlowDefinitionCmdExe;
    private FindFlowDefinitionByIdQryExe findFlowDefinitionByIdQryExe;
    private FlowDefinitionPageQryExe flowDefinitionPageQryExe;

    @BeforeEach
    void setUp() {
        createFlowDefinitionCmdExe = mock(CreateFlowDefinitionCmdExe.class);
        UpdateFlowDefinitionCmdExe updateFlowDefinitionCmdExe = mock(UpdateFlowDefinitionCmdExe.class);
        DeleteFlowDefinitionCmdExe deleteFlowDefinitionCmdExe = mock(DeleteFlowDefinitionCmdExe.class);
        CopyFlowDefinitionCmdExe copyFlowDefinitionCmdExe = mock(CopyFlowDefinitionCmdExe.class);
        publishFlowDefinitionCmdExe = mock(PublishFlowDefinitionCmdExe.class);
        CancelPublishFlowDefinitionCmdExe cancelPublishFlowDefinitionCmdExe =
                mock(CancelPublishFlowDefinitionCmdExe.class);
        ActivateFlowDefinitionCmdExe activateFlowDefinitionCmdExe = mock(ActivateFlowDefinitionCmdExe.class);
        SuspendFlowDefinitionCmdExe suspendFlowDefinitionCmdExe = mock(SuspendFlowDefinitionCmdExe.class);
        SyncFlowDefinitionTenantCmdExe syncFlowDefinitionTenantCmdExe = mock(SyncFlowDefinitionTenantCmdExe.class);
        findFlowDefinitionByIdQryExe = mock(FindFlowDefinitionByIdQryExe.class);
        flowDefinitionPageQryExe = mock(FlowDefinitionPageQryExe.class);
        ListFlowDefinitionQryExe listFlowDefinitionQryExe = mock(ListFlowDefinitionQryExe.class);
        ListUnpublishedFlowDefinitionQryExe listUnpublishedFlowDefinitionQryExe =
                mock(ListUnpublishedFlowDefinitionQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new FlowDefinitionController(
                        createFlowDefinitionCmdExe,
                        updateFlowDefinitionCmdExe,
                        deleteFlowDefinitionCmdExe,
                        copyFlowDefinitionCmdExe,
                        publishFlowDefinitionCmdExe,
                        cancelPublishFlowDefinitionCmdExe,
                        activateFlowDefinitionCmdExe,
                        suspendFlowDefinitionCmdExe,
                        syncFlowDefinitionTenantCmdExe,
                        findFlowDefinitionByIdQryExe,
                        flowDefinitionPageQryExe,
                        listFlowDefinitionQryExe,
                        listUnpublishedFlowDefinitionQryExe))
                .build();
    }

    @Test
    void page_should_return_definitions() throws Exception {
        when(flowDefinitionPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(definition()), 1));

        mockMvc.perform(get(FlowDefinitionController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].flowCode").value("leave"));
    }

    @Test
    void create_should_return_saved_definition() throws Exception {
        when(createFlowDefinitionCmdExe.execute(any())).thenReturn(definition());

        mockMvc.perform(post(FlowDefinitionController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flowCode\":\"leave\",\"flowName\":\"Leave\",\"modelType\":\"JSON\","
                                + "\"tenantId\":\"000000\",\"definitionJson\":\"{\\\"assigneeConfig\\\":{}}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void publish_should_return_published_definition() throws Exception {
        FlowDefinition definition = definition();
        definition.setPublishStatus(1);
        when(publishFlowDefinitionCmdExe.execute(any())).thenReturn(definition);

        mockMvc.perform(post(FlowDefinitionController.BASE_PATH + "/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publishStatus").value(1));
    }

    @Test
    void json_should_return_definition_json() throws Exception {
        when(findFlowDefinitionByIdQryExe.execute(any())).thenReturn(Optional.of(definition()));

        mockMvc.perform(get(FlowDefinitionController.BASE_PATH + "/1/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("{\"assigneeConfig\":{}}"));
    }

    private FlowDefinition definition() {
        FlowDefinition definition = new FlowDefinition();
        definition.setId(1L);
        definition.setFlowCode("leave");
        definition.setFlowName("Leave");
        definition.setModelType("JSON");
        definition.setVersion(1);
        definition.setPublishStatus(0);
        definition.setCustomForm(false);
        definition.setActiveStatus(1);
        definition.setTenantId("000000");
        definition.setDefinitionJson("{\"assigneeConfig\":{}}");
        definition.setDefinitionXml("<process />");
        return definition;
    }
}
