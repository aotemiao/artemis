package com.aotemiao.artemis.resource.infra.gateway.message;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.resource.domain.gateway.message.SystemMessageGateway;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import com.aotemiao.artemis.resource.infra.converter.message.SystemMessageConverter;
import com.aotemiao.artemis.resource.infra.repository.message.SystemMessageRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemMessageGatewayImpl implements SystemMessageGateway {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects repository as a managed collaborator; this gateway does not expose it.")
    private final SystemMessageRepository repository;

    public SystemMessageGatewayImpl(SystemMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemMessage save(SystemMessage message) {
        return SystemMessageConverter.toDomain(repository.save(SystemMessageConverter.toDO(message)));
    }

    @Override
    public Optional<SystemMessage> findById(Long id) {
        return repository
                .findById(id)
                .filter(entity -> Integer.valueOf(0).equals(entity.getDeleted()))
                .map(SystemMessageConverter::toDomain);
    }

    @Override
    public PageResult<SystemMessage> findInbox(Long recipientUserId, PageRequest pageRequest) {
        var page = repository.findAllByRecipientUserIdAndDeletedOrderByIdDesc(
                recipientUserId, 0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(SystemMessageConverter::toDomain).toList(),
                pr.totalPages());
    }
}
