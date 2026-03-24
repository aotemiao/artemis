package com.aotemiao.artemis.system.domain.gateway;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import java.util.Optional;

/** 系统用户目录 Gateway。 */
public interface SystemUserGateway {

    SystemUser save(SystemUser systemUser);

    Optional<SystemUser> findById(Long id);

    Optional<SystemUser> findByUsername(String username);

    PageResult<SystemUser> findPage(PageRequest pageRequest);
}
