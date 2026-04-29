package com.aotemiao.artemis.workflow.app.query.definition;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 流程定义分页查询执行器。 */
@Component
public class FlowDefinitionPageQryExe {

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    public FlowDefinitionPageQryExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public PageResult<FlowDefinition> execute(FlowDefinitionPageQry qry) {
        return flowDefinitionGateway.findPage(qry.pageRequest());
    }
}
