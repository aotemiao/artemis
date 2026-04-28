package com.aotemiao.artemis.system.domain.gateway.notice;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import java.util.Optional;

/** 系统通知公告 Gateway。 */
public interface SystemNoticeGateway {

    SystemNotice save(SystemNotice systemNotice);

    Optional<SystemNotice> findById(Long id);

    PageResult<SystemNotice> findPage(PageRequest pageRequest);

    void deleteById(Long id);
}
