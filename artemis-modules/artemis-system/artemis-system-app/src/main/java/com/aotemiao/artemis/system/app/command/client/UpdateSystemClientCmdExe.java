package com.aotemiao.artemis.system.app.command.client;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 更新系统客户端命令执行器。 */
@Component
public class UpdateSystemClientCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemClientGateway systemClientGateway;

    public UpdateSystemClientCmdExe(SystemClientGateway systemClientGateway) {
        this.systemClientGateway = systemClientGateway;
    }

    public SystemClient execute(UpdateSystemClientCmd cmd) {
        SystemClient current = systemClientGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Client not found: " + cmd.id()));
        ensureUnique(cmd.clientId(), cmd.clientKey(), cmd.id());
        current.setClientId(cmd.clientId());
        current.setClientKey(cmd.clientKey());
        current.setClientSecret(cmd.clientSecret());
        current.setGrantTypes(cmd.grantTypes());
        current.setDeviceType(cmd.deviceType());
        current.setActiveTimeoutSeconds(cmd.activeTimeoutSeconds());
        current.setFixedTimeoutSeconds(cmd.fixedTimeoutSeconds());
        current.setStatus(cmd.status());
        current.setRemarks(cmd.remarks());
        return systemClientGateway.save(current);
    }

    private void ensureUnique(String clientId, String clientKey, Long currentId) {
        systemClientGateway.findByClientId(clientId).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Client id already exists: " + clientId);
            }
        });
        systemClientGateway.findByClientKey(clientKey).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Client key already exists: " + clientKey);
            }
        });
    }
}
