package com.aotemiao.artemis.system.infra.gateway.client;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import com.aotemiao.artemis.system.infra.converter.client.SystemClientConverter;
import com.aotemiao.artemis.system.infra.repository.client.SystemClientRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemClientGatewayImpl implements SystemClientGateway {

    private final SystemClientRepository repository;

    public SystemClientGatewayImpl(SystemClientRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemClient save(SystemClient systemClient) {
        return SystemClientConverter.toDomain(repository.save(SystemClientConverter.toDO(systemClient)));
    }

    @Override
    public Optional<SystemClient> findById(Long id) {
        return repository.findById(id).filter(d -> d.getDeleted() == 0).map(SystemClientConverter::toDomain);
    }

    @Override
    public Optional<SystemClient> findByClientId(String clientId) {
        return repository.findByClientIdAndDeleted(clientId, 0).map(SystemClientConverter::toDomain);
    }

    @Override
    public Optional<SystemClient> findByClientKey(String clientKey) {
        return repository.findByClientKeyAndDeleted(clientKey, 0).map(SystemClientConverter::toDomain);
    }

    @Override
    public PageResult<SystemClient> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderById(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(SystemClientConverter::toDomain).toList(),
                pr.totalPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeleted(1);
            repository.save(entity);
        });
    }
}
