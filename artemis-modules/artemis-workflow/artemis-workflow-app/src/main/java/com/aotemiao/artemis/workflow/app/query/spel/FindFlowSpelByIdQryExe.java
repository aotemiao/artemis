package com.aotemiao.artemis.workflow.app.query.spel;

import com.aotemiao.artemis.workflow.domain.gateway.spel.FlowSpelGateway;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询流程 SpEL 表达式执行器。 */
@Component
public class FindFlowSpelByIdQryExe {

    private final FlowSpelGateway flowSpelGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    public FindFlowSpelByIdQryExe(FlowSpelGateway flowSpelGateway) {
        this.flowSpelGateway = flowSpelGateway;
    }

    public Optional<FlowSpel> execute(FindFlowSpelByIdQry qry) {
        return flowSpelGateway.findById(qry.id());
    }
}
