package com.aotemiao.artemis.workflow.app.command.spel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import org.junit.jupiter.api.Test;

class FlowSpelCmdExeTest {

    @Test
    void create_should_default_status_and_save_spel() {
        FakeFlowSpelGateway gateway = new FakeFlowSpelGateway();
        CreateFlowSpelCmdExe createExe = new CreateFlowSpelCmdExe(gateway);

        FlowSpel spel = createExe.execute(new CreateFlowSpelCmd(new FlowSpelPayload(
                "deptResolver", "leader", "#deptId", "#{deptResolver.leader(#deptId)}", null, null)));

        assertThat(spel.getId()).isEqualTo(1L);
        assertThat(spel.getStatus()).isEqualTo(1);
        assertThat(spel.getPreviewExpression()).isEqualTo("#{deptResolver.leader(#deptId)}");
    }

    @Test
    void create_should_reject_duplicate_preview_expression() {
        FakeFlowSpelGateway gateway = new FakeFlowSpelGateway();
        CreateFlowSpelCmdExe createExe = new CreateFlowSpelCmdExe(gateway);
        FlowSpelPayload payload =
                new FlowSpelPayload("starter", "userId", "$userId", "#{starter.userId($userId)}", null, 1);
        createExe.execute(new CreateFlowSpelCmd(payload));

        assertThatThrownBy(() -> createExe.execute(new CreateFlowSpelCmd(payload)))
                .isInstanceOf(BizException.class);
    }

    @Test
    void update_should_allow_current_preview_expression() {
        FakeFlowSpelGateway gateway = new FakeFlowSpelGateway();
        CreateFlowSpelCmdExe createExe = new CreateFlowSpelCmdExe(gateway);
        FlowSpel spel = createExe.execute(new CreateFlowSpelCmd(
                new FlowSpelPayload("starter", "userId", "$userId", "#{starter.userId($userId)}", null, 1)));
        UpdateFlowSpelCmdExe updateExe = new UpdateFlowSpelCmdExe(gateway);

        FlowSpel updated = updateExe.execute(new UpdateFlowSpelCmd(
                spel.getId(),
                new FlowSpelPayload("starter", "userId", "$userId", "#{starter.userId($userId)}", "updated", 0)));

        assertThat(updated.getRemarks()).isEqualTo("updated");
        assertThat(updated.getStatus()).isZero();
    }
}
