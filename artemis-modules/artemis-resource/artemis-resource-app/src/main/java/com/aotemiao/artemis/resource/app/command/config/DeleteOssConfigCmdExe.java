package com.aotemiao.artemis.resource.app.command.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.config.OssConfigGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;

@Service
public class DeleteOssConfigCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final OssConfigGateway ossConfigGateway;

    public DeleteOssConfigCmdExe(OssConfigGateway ossConfigGateway) {
        this.ossConfigGateway = ossConfigGateway;
    }

    public void execute(DeleteOssConfigCmd cmd) {
        var config = ossConfigGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Oss config not found: " + cmd.id()));
        if (config.isBuiltIn()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Built-in oss config cannot be deleted");
        }
        ossConfigGateway.deleteById(cmd.id());
    }
}
