package com.aotemiao.artemis.workflow.app.query.definition;

import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 流程定义列表查询执行器。 */
@Component
public class ListFlowDefinitionQryExe {

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    public ListFlowDefinitionQryExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public List<FlowDefinition> execute(ListFlowDefinitionQry qry) {
        return flowDefinitionGateway.findAll();
    }
}
