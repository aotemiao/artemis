package com.aotemiao.artemis.resource.app.command.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.config.OssConfigGateway;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetDefaultOssConfigCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final OssConfigGateway ossConfigGateway;

    public SetDefaultOssConfigCmdExe(OssConfigGateway ossConfigGateway) {
        this.ossConfigGateway = ossConfigGateway;
    }

    @Transactional(rollbackFor = Exception.class)
    public OssConfig execute(SetDefaultOssConfigCmd cmd) {
        OssConfig config = ossConfigGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Oss config not found: " + cmd.id()));
        if (!config.isEnabled()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Disabled oss config cannot be default");
        }
        config.setDefaultFlag(1);
        OssConfig saved = ossConfigGateway.save(config);
        ossConfigGateway.clearDefaultExcept(saved.getId());
        return saved;
    }
}
