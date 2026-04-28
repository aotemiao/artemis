package com.aotemiao.artemis.system.domain.gateway.audit;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import java.util.Collection;
import java.util.Optional;

/** 后台操作日志 Gateway。 */
public interface OperLogGateway {

    OperLog save(OperLog operLog);

    Optional<OperLog> findById(Long id);

    PageResult<OperLog> findPage(PageRequest pageRequest);

    void deleteByIds(Collection<Long> ids);

    void clear();
}
