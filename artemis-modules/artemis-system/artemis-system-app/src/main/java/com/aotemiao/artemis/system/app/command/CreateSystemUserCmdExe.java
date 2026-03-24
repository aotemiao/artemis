package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 新增系统用户命令执行器。 */
@Component
public class CreateSystemUserCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemUserGateway systemUserGateway;

    public CreateSystemUserCmdExe(SystemUserGateway systemUserGateway) {
        this.systemUserGateway = systemUserGateway;
    }

    public SystemUser execute(CreateSystemUserCmd cmd) {
        systemUserGateway.findByUsername(cmd.username()).ifPresent(existing -> {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Username already exists: " + cmd.username());
        });

        SystemUser systemUser = new SystemUser();
        systemUser.setUsername(cmd.username());
        systemUser.setDisplayName(cmd.displayName());
        systemUser.setPassword(cmd.password());
        systemUser.setEnabled(true);
        return systemUserGateway.save(systemUser);
    }
}
