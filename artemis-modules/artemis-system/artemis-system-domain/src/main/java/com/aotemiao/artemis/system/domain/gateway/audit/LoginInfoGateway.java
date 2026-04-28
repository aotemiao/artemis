package com.aotemiao.artemis.system.domain.gateway.audit;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import java.util.Collection;
import java.util.Optional;

/** 登录访问日志 Gateway。 */
public interface LoginInfoGateway {

    LoginInfo save(LoginInfo loginInfo);

    Optional<LoginInfo> findById(Long id);

    PageResult<LoginInfo> findPage(PageRequest pageRequest);

    void deleteByIds(Collection<Long> ids);

    void clear();
}
