package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FindLookupTypeByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LookupTypeGateway lookupTypeGateway;

    public FindLookupTypeByIdQryExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    public Optional<LookupType> execute(FindLookupTypeByIdQry qry) {
        return lookupTypeGateway.findById(qry.id());
    }
}
