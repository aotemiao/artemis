package com.aotemiao.artemis.resource.app.query.config;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.resource.domain.gateway.config.OssConfigGateway;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;

@Service
public class OssConfigPageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final OssConfigGateway ossConfigGateway;

    public OssConfigPageQryExe(OssConfigGateway ossConfigGateway) {
        this.ossConfigGateway = ossConfigGateway;
    }

    public PageResult<OssConfig> execute(OssConfigPageQry qry) {
        return ossConfigGateway.findPage(qry.pageRequest());
    }
}
