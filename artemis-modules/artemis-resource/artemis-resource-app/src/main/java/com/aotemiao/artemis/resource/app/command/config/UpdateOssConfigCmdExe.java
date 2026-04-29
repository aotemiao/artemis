package com.aotemiao.artemis.resource.app.command.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.config.OssConfigGateway;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOssConfigCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final OssConfigGateway ossConfigGateway;

    public UpdateOssConfigCmdExe(OssConfigGateway ossConfigGateway) {
        this.ossConfigGateway = ossConfigGateway;
    }

    @Transactional(rollbackFor = Exception.class)
    public OssConfig execute(UpdateOssConfigCmd cmd) {
        OssConfig config = ossConfigGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Oss config not found: " + cmd.id()));
        OssConfigAssembler.apply(config, cmd.payload());
        ossConfigGateway.findByConfigKey(config.getConfigKey()).ifPresent(existing -> {
            if (!existing.getId().equals(cmd.id())) {
                throw new BizException(
                        CommonErrorCode.BAD_REQUEST, "Oss config key already exists: " + existing.getConfigKey());
            }
        });
        OssConfig saved = ossConfigGateway.save(config);
        if (saved.isDefault()) {
            ossConfigGateway.clearDefaultExcept(saved.getId());
        }
        return saved;
    }
}
