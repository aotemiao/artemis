package com.aotemiao.artemis.system.infra.gateway.auth;

import com.aotemiao.artemis.system.domain.gateway.auth.UserCredentialsGateway;
import com.aotemiao.artemis.system.infra.repository.user.SystemUserRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 用户凭证校验 Gateway 实现。 当前直接复用系统用户表完成最小凭证校验。 */
@Component
public class UserCredentialsGatewayImpl implements UserCredentialsGateway {

    private final SystemUserRepository systemUserRepository;

    public UserCredentialsGatewayImpl(SystemUserRepository systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    @Override
    public Optional<Long> validate(String tenantNo, String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return systemUserRepository
                .findByTenantNoAndUsernameAndDeleted(tenantNo, username, 0)
                .filter(systemUserDO -> systemUserDO.isEnabled() && password.equals(systemUserDO.getPassword()))
                .map(systemUserDO -> systemUserDO.getId());
    }
}
