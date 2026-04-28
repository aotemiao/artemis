package com.aotemiao.artemis.system.app.command.client;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 创建系统客户端命令执行器。 */
@Component
public class CreateSystemClientCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemClientGateway systemClientGateway;

    public CreateSystemClientCmdExe(SystemClientGateway systemClientGateway) {
        this.systemClientGateway = systemClientGateway;
    }

    public SystemClient execute(CreateSystemClientCmd cmd) {
        ensureUnique(cmd.clientId(), cmd.clientKey(), null);
        SystemClient client = new SystemClient();
        client.setClientId(cmd.clientId());
        client.setClientKey(cmd.clientKey());
        client.setClientSecret(cmd.clientSecret());
        client.setGrantTypes(cmd.grantTypes());
        client.setDeviceType(cmd.deviceType());
        client.setActiveTimeoutSeconds(cmd.activeTimeoutSeconds());
        client.setFixedTimeoutSeconds(cmd.fixedTimeoutSeconds());
        client.setStatus(cmd.status());
        client.setRemarks(cmd.remarks());
        return systemClientGateway.save(client);
    }

    private void ensureUnique(String clientId, String clientKey, Long currentId) {
        systemClientGateway.findByClientId(clientId).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Client id already exists: " + clientId);
            }
        });
        systemClientGateway.findByClientKey(clientKey).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Client key already exists: " + clientKey);
            }
        });
    }
}
