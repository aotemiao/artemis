package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupType;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FindLookupTypeByIdQryExe {

    private final LookupTypeGateway lookupTypeGateway;

    public FindLookupTypeByIdQryExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    public Optional<LookupType> execute(FindLookupTypeByIdQry qry) {
        return lookupTypeGateway.findById(qry.id());
    }
}
