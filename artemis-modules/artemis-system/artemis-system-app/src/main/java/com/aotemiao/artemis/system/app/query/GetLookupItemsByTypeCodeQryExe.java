package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetLookupItemsByTypeCodeQryExe {

    private final LookupTypeGateway lookupTypeGateway;

    public GetLookupItemsByTypeCodeQryExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    public List<LookupItem> execute(GetLookupItemsByTypeCodeQry qry) {
        return lookupTypeGateway.findItemsByTypeCode(qry.typeCode());
    }
}
