package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetLookupItemsByTypeCodeQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LookupTypeGateway lookupTypeGateway;

    public GetLookupItemsByTypeCodeQryExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    public List<LookupItem> execute(GetLookupItemsByTypeCodeQry qry) {
        return lookupTypeGateway.findItemsByTypeCode(qry.typeCode());
    }
}
