package com.aotemiao.artemis.workflow.app.query.definition;

import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 流程定义详情查询执行器。 */
@Component
public class FindFlowDefinitionByIdQryExe {

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    public FindFlowDefinitionByIdQryExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public Optional<FlowDefinition> execute(FindFlowDefinitionByIdQry qry) {
        return flowDefinitionGateway.findById(qry.id());
    }
}
