package com.aotemiao.artemis.resource.domain.gateway.message;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import java.util.Optional;

public interface SystemMessageGateway {

    SystemMessage save(SystemMessage message);

    Optional<SystemMessage> findById(Long id);

    PageResult<SystemMessage> findInbox(Long recipientUserId, PageRequest pageRequest);
}
