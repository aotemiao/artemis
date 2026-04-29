package com.aotemiao.artemis.workflow.app.query.category;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.domain.gateway.category.FlowCategoryGateway;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 流程分类分页查询执行器。 */
@Component
public class FlowCategoryPageQryExe {

    private final FlowCategoryGateway flowCategoryGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    public FlowCategoryPageQryExe(FlowCategoryGateway flowCategoryGateway) {
        this.flowCategoryGateway = flowCategoryGateway;
    }

    public PageResult<FlowCategory> execute(FlowCategoryPageQry qry) {
        return flowCategoryGateway.findPage(qry.pageRequest());
    }
}
