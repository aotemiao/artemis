package com.aotemiao.artemis.resource.app.command.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.config.OssConfigGateway;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;

@Service
public class ChangeOssConfigStatusCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final OssConfigGateway ossConfigGateway;

    public ChangeOssConfigStatusCmdExe(OssConfigGateway ossConfigGateway) {
        this.ossConfigGateway = ossConfigGateway;
    }

    public OssConfig execute(ChangeOssConfigStatusCmd cmd) {
        OssConfig config = ossConfigGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Oss config not found: " + cmd.id()));
        config.setStatus(OssConfigAssembler.normalizeStatus(cmd.status()));
        if (!config.isEnabled()) {
            config.setDefaultFlag(0);
        }
        return ossConfigGateway.save(config);
    }
}
