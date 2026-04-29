package com.aotemiao.artemis.resource.app.command.message;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.resource.domain.gateway.message.SystemMessageGateway;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class FakeSystemMessageGateway implements SystemMessageGateway {

    private final Map<Long, SystemMessage> storage = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public SystemMessage save(SystemMessage message) {
        if (message.getId() == null) {
            message.setId(nextId++);
        }
        storage.put(message.getId(), message);
        return message;
    }

    @Override
    public Optional<SystemMessage> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public PageResult<SystemMessage> findInbox(Long recipientUserId, PageRequest pageRequest) {
        var content = storage.values().stream()
                .filter(message -> message.getRecipientUserId().equals(recipientUserId))
                .toList();
        return PageResult.of(content.size(), content, 1);
    }
}
