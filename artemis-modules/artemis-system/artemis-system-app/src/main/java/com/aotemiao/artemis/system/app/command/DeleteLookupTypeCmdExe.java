package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import org.springframework.stereotype.Component;

@Component
public class DeleteLookupTypeCmdExe {

    private final LookupTypeGateway lookupTypeGateway;

    public DeleteLookupTypeCmdExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    /** 若 LookupType 不存在则抛 NOT_FOUND(404)，否则执行逻辑删除。 */
    public void execute(DeleteLookupTypeCmd cmd) {
        lookupTypeGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "LookupType not found: " + cmd.id()));
        lookupTypeGateway.deleteById(cmd.id());
    }
}
