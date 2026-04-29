package com.aotemiao.artemis.workflow.app.query.spel;

import com.aotemiao.artemis.workflow.domain.gateway.spel.FlowSpelGateway;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 查询流程 SpEL 表达式列表执行器。 */
@Component
public class ListFlowSpelQryExe {

    private final FlowSpelGateway flowSpelGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    public ListFlowSpelQryExe(FlowSpelGateway flowSpelGateway) {
        this.flowSpelGateway = flowSpelGateway;
    }

    public List<FlowSpel> execute(ListFlowSpelQry qry) {
        return flowSpelGateway.findAll();
    }
}
