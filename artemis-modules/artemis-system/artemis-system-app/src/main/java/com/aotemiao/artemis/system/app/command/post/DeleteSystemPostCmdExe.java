package com.aotemiao.artemis.system.app.command.post;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 删除系统岗位命令执行器。 */
@Component
public class DeleteSystemPostCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemPostGateway systemPostGateway;

    public DeleteSystemPostCmdExe(SystemPostGateway systemPostGateway) {
        this.systemPostGateway = systemPostGateway;
    }

    public void execute(DeleteSystemPostCmd cmd) {
        systemPostGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Post not found: " + cmd.id()));
        if (systemPostGateway.countUsersByPostId(cmd.id()) > 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Assigned post cannot be deleted: " + cmd.id());
        }
        systemPostGateway.deleteById(cmd.id());
    }
}
