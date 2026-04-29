package com.aotemiao.artemis.workflow.app.query.category;

import com.aotemiao.artemis.workflow.domain.gateway.category.FlowCategoryGateway;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 查询流程分类列表执行器。 */
@Component
public class ListFlowCategoryQryExe {

    private final FlowCategoryGateway flowCategoryGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    public ListFlowCategoryQryExe(FlowCategoryGateway flowCategoryGateway) {
        this.flowCategoryGateway = flowCategoryGateway;
    }

    public List<FlowCategory> execute(ListFlowCategoryQry qry) {
        return flowCategoryGateway.findAll();
    }
}
