package com.aotemiao.artemis.workflow.app.command.definition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import org.junit.jupiter.api.Test;

class FlowDefinitionCmdExeTest {

    @Test
    void create_should_default_status_and_version() {
        FakeFlowDefinitionGateway gateway = new FakeFlowDefinitionGateway();
        CreateFlowDefinitionCmdExe createExe = new CreateFlowDefinitionCmdExe(gateway);

        FlowDefinition definition = createExe.execute(new CreateFlowDefinitionCmd(payload("leave")));

        assertThat(definition.getId()).isEqualTo(1L);
        assertThat(definition.getVersion()).isEqualTo(1);
        assertThat(definition.getPublishStatus()).isZero();
        assertThat(definition.getActiveStatus()).isEqualTo(1);
    }

    @Test
    void publish_should_reject_definition_without_assignee_config() {
        FakeFlowDefinitionGateway gateway = new FakeFlowDefinitionGateway();
        FlowDefinition definition =
                new CreateFlowDefinitionCmdExe(gateway).execute(new CreateFlowDefinitionCmd(payload("leave")));
        PublishFlowDefinitionCmdExe publishExe = new PublishFlowDefinitionCmdExe(gateway);

        assertThatThrownBy(() -> publishExe.execute(new ChangeFlowDefinitionStateCmd(definition.getId())))
                .isInstanceOf(BizException.class);
    }

    @Test
    void publish_should_mark_definition_published_when_assignee_exists() {
        FakeFlowDefinitionGateway gateway = new FakeFlowDefinitionGateway();
        FlowDefinitionPayload payload = new FlowDefinitionPayload(
                "leave",
                "Leave",
                "JSON",
                null,
                null,
                false,
                null,
                null,
                null,
                "000000",
                "{\"nodes\":[{\"type\":\"approval\",\"assigneeConfig\":{\"userIds\":[1]}}]}",
                "<process />");
        FlowDefinition definition =
                new CreateFlowDefinitionCmdExe(gateway).execute(new CreateFlowDefinitionCmd(payload));
        PublishFlowDefinitionCmdExe publishExe = new PublishFlowDefinitionCmdExe(gateway);

        FlowDefinition published = publishExe.execute(new ChangeFlowDefinitionStateCmd(definition.getId()));

        assertThat(published.getPublishStatus()).isEqualTo(1);
    }

    @Test
    void copy_should_create_unpublished_new_definition() {
        FakeFlowDefinitionGateway gateway = new FakeFlowDefinitionGateway();
        FlowDefinition source =
                new CreateFlowDefinitionCmdExe(gateway).execute(new CreateFlowDefinitionCmd(payload("leave")));
        CopyFlowDefinitionCmdExe copyExe = new CopyFlowDefinitionCmdExe(gateway);

        FlowDefinition copied =
                copyExe.execute(new CopyFlowDefinitionCmd(source.getId(), "leave_copy", "Leave Copy", null));

        assertThat(copied.getId()).isEqualTo(2L);
        assertThat(copied.getFlowCode()).isEqualTo("leave_copy");
        assertThat(copied.getPublishStatus()).isZero();
    }

    @Test
    void syncTenant_should_copy_definition_to_new_tenant() {
        FakeFlowDefinitionGateway gateway = new FakeFlowDefinitionGateway();
        FlowDefinition source =
                new CreateFlowDefinitionCmdExe(gateway).execute(new CreateFlowDefinitionCmd(payload("leave")));
        SyncFlowDefinitionTenantCmdExe syncExe = new SyncFlowDefinitionTenantCmdExe(gateway);

        FlowDefinition synced = syncExe.execute(new SyncFlowDefinitionTenantCmd(source.getId(), "100001"));

        assertThat(synced.getId()).isEqualTo(2L);
        assertThat(synced.getFlowCode()).isEqualTo("leave");
        assertThat(synced.getTenantId()).isEqualTo("100001");
        assertThat(synced.getPublishStatus()).isZero();
    }

    @Test
    void delete_should_reject_used_definition() {
        FakeFlowDefinitionGateway gateway = new FakeFlowDefinitionGateway();
        FlowDefinition definition =
                new CreateFlowDefinitionCmdExe(gateway).execute(new CreateFlowDefinitionCmd(payload("leave")));
        gateway.markUsedByInstance();
        DeleteFlowDefinitionCmdExe deleteExe = new DeleteFlowDefinitionCmdExe(gateway);

        assertThatThrownBy(() -> deleteExe.execute(new DeleteFlowDefinitionCmd(definition.getId())))
                .isInstanceOf(BizException.class);
    }

    private FlowDefinitionPayload payload(String flowCode) {
        return new FlowDefinitionPayload(
                flowCode,
                "Leave",
                "JSON",
                null,
                null,
                false,
                "/leave/form",
                null,
                null,
                "000000",
                "{\"nodes\":[{\"type\":\"approval\"}]}",
                "<process />");
    }
}
