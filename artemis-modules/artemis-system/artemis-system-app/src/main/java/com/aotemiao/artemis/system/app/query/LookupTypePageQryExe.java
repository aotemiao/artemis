package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.LookupType;
import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import org.springframework.stereotype.Component;

@Component
public class LookupTypePageQryExe {

    private final LookupTypeGateway lookupTypeGateway;

    public LookupTypePageQryExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    public PageResult<LookupType> execute(LookupTypePageQry qry) {
        return lookupTypeGateway.findPage(qry.pageRequest());
    }
}
