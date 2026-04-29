package com.aotemiao.artemis.resource.app.query.message;

import com.aotemiao.artemis.framework.core.domain.PageRequest;

public record SystemMessageInboxQry(Long recipientUserId, PageRequest pageRequest) {}
