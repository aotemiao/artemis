package com.aotemiao.artemis.workflow.app.query.category;

import com.aotemiao.artemis.workflow.domain.gateway.category.FlowCategoryGateway;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询流程分类执行器。 */
@Component
public class FindFlowCategoryByIdQryExe {

    private final FlowCategoryGateway flowCategoryGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    public FindFlowCategoryByIdQryExe(FlowCategoryGateway flowCategoryGateway) {
        this.flowCategoryGateway = flowCategoryGateway;
    }

    public Optional<FlowCategory> execute(FindFlowCategoryByIdQry qry) {
        return flowCategoryGateway.findById(qry.id());
    }
}
