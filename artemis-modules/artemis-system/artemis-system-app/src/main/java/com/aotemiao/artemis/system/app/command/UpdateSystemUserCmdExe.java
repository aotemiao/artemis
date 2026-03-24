package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 更新系统用户命令执行器。 */
@Component
public class UpdateSystemUserCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemUserGateway systemUserGateway;

    public UpdateSystemUserCmdExe(SystemUserGateway systemUserGateway) {
        this.systemUserGateway = systemUserGateway;
    }

    public SystemUser execute(UpdateSystemUserCmd cmd) {
        SystemUser systemUser = systemUserGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemUser not found: " + cmd.id()));
        systemUser.setDisplayName(cmd.displayName());
        if (cmd.password() != null && !cmd.password().isBlank()) {
            systemUser.setPassword(cmd.password());
        }
        systemUser.setEnabled(Boolean.TRUE.equals(cmd.enabled()));
        return systemUserGateway.save(systemUser);
    }
}
