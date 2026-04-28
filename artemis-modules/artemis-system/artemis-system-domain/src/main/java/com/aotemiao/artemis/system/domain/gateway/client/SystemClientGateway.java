package com.aotemiao.artemis.system.domain.gateway.client;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import java.util.Optional;

/** 系统客户端 Gateway。 */
public interface SystemClientGateway {

    SystemClient save(SystemClient systemClient);

    Optional<SystemClient> findById(Long id);

    Optional<SystemClient> findByClientId(String clientId);

    Optional<SystemClient> findByClientKey(String clientKey);

    PageResult<SystemClient> findPage(PageRequest pageRequest);

    void deleteById(Long id);
}
