package com.aotemiao.artemis.resource.app.query.config;

import com.aotemiao.artemis.resource.domain.gateway.config.OssConfigGateway;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FindOssConfigByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final OssConfigGateway ossConfigGateway;

    public FindOssConfigByIdQryExe(OssConfigGateway ossConfigGateway) {
        this.ossConfigGateway = ossConfigGateway;
    }

    public Optional<OssConfig> execute(FindOssConfigByIdQry qry) {
        return ossConfigGateway.findById(qry.id());
    }
}
