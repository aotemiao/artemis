package com.aotemiao.artemis.system.app.command.auth;

import com.aotemiao.artemis.system.app.service.tenant.TenantRuntimeService;
import com.aotemiao.artemis.system.domain.gateway.auth.UserCredentialsGateway;
import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 校验用户凭证命令执行器。 */
@Component
public class ValidateCredentialsCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final UserCredentialsGateway userCredentialsGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemClientGateway systemClientGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the service as a managed collaborator; this executor does not expose it.")
    private final TenantRuntimeService tenantRuntimeService;

    public ValidateCredentialsCmdExe(
            UserCredentialsGateway userCredentialsGateway,
            SystemClientGateway systemClientGateway,
            TenantRuntimeService tenantRuntimeService) {
        this.userCredentialsGateway = userCredentialsGateway;
        this.systemClientGateway = systemClientGateway;
        this.tenantRuntimeService = tenantRuntimeService;
    }

    /**
     * 校验用户名与密码，返回对应用户 ID。
     *
     * @param cmd 用户名与密码
     * @return 校验通过时返回 userId，否则 empty
     */
    public Optional<Long> execute(ValidateCredentialsCmd cmd) {
        boolean clientAllowed = systemClientGateway
                .findByClientId(cmd.clientId())
                .filter(client -> client.isNormal() && client.supportsGrantType(cmd.grantType()))
                .isPresent();
        if (!clientAllowed) {
            return Optional.empty();
        }
        String tenantNo = tenantRuntimeService.normalizeTenantNo(cmd.tenantId());
        if (!tenantRuntimeService.canLogin(tenantNo)) {
            return Optional.empty();
        }
        return userCredentialsGateway.validate(tenantNo, cmd.username(), cmd.password());
    }
}
