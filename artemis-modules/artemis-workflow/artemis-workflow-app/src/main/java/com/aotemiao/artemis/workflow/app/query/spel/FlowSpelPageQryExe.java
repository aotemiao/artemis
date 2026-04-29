package com.aotemiao.artemis.workflow.app.query.spel;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.domain.gateway.spel.FlowSpelGateway;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 流程 SpEL 表达式分页查询执行器。 */
@Component
public class FlowSpelPageQryExe {

    private final FlowSpelGateway flowSpelGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    public FlowSpelPageQryExe(FlowSpelGateway flowSpelGateway) {
        this.flowSpelGateway = flowSpelGateway;
    }

    public PageResult<FlowSpel> execute(FlowSpelPageQry qry) {
        return flowSpelGateway.findPage(qry.pageRequest());
    }
}
