package com.aotemiao.artemis.workflow.app.command.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import org.junit.jupiter.api.Test;

class FlowCategoryCmdExeTest {

    @Test
    void create_should_save_root_and_child_with_ancestors() {
        FakeFlowCategoryGateway gateway = new FakeFlowCategoryGateway();
        CreateFlowCategoryCmdExe createExe = new CreateFlowCategoryCmdExe(gateway);

        FlowCategory root =
                createExe.execute(new CreateFlowCategoryCmd(new FlowCategoryPayload(0L, "审批流程", 1, "root")));
        FlowCategory child =
                createExe.execute(new CreateFlowCategoryCmd(new FlowCategoryPayload(root.getId(), "请假流程", 2, "child")));

        assertThat(root.getAncestors()).isEqualTo("0");
        assertThat(child.getParentId()).isEqualTo(root.getId());
        assertThat(child.getAncestors()).isEqualTo("0," + root.getId());
    }

    @Test
    void update_should_reject_top_level_parent_change() {
        FakeFlowCategoryGateway gateway = new FakeFlowCategoryGateway();
        CreateFlowCategoryCmdExe createExe = new CreateFlowCategoryCmdExe(gateway);
        FlowCategory root = createExe.execute(new CreateFlowCategoryCmd(new FlowCategoryPayload(0L, "审批流程", 1, null)));
        FlowCategory other = createExe.execute(new CreateFlowCategoryCmd(new FlowCategoryPayload(0L, "业务流程", 2, null)));
        UpdateFlowCategoryCmdExe updateExe = new UpdateFlowCategoryCmdExe(gateway);

        assertThatThrownBy(() -> updateExe.execute(new UpdateFlowCategoryCmd(
                        root.getId(), new FlowCategoryPayload(other.getId(), "审批流程", 1, null))))
                .isInstanceOf(BizException.class);
    }

    @Test
    void delete_should_reject_category_with_children() {
        FakeFlowCategoryGateway gateway = new FakeFlowCategoryGateway();
        CreateFlowCategoryCmdExe createExe = new CreateFlowCategoryCmdExe(gateway);
        FlowCategory root = createExe.execute(new CreateFlowCategoryCmd(new FlowCategoryPayload(0L, "审批流程", 1, null)));
        createExe.execute(new CreateFlowCategoryCmd(new FlowCategoryPayload(root.getId(), "请假流程", 2, null)));
        DeleteFlowCategoryCmdExe deleteExe = new DeleteFlowCategoryCmdExe(gateway);

        assertThatThrownBy(() -> deleteExe.execute(new DeleteFlowCategoryCmd(root.getId())))
                .isInstanceOf(BizException.class);
    }
}
