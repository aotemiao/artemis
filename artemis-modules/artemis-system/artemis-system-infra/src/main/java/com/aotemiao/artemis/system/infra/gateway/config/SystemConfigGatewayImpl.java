package com.aotemiao.artemis.system.infra.gateway.config;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import com.aotemiao.artemis.system.infra.converter.config.SystemConfigConverter;
import com.aotemiao.artemis.system.infra.repository.config.SystemConfigRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemConfigGatewayImpl implements SystemConfigGateway {

    private final SystemConfigRepository repository;

    public SystemConfigGatewayImpl(SystemConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemConfig save(SystemConfig systemConfig) {
        return SystemConfigConverter.toDomain(repository.save(SystemConfigConverter.toDO(systemConfig)));
    }

    @Override
    public Optional<SystemConfig> findById(Long id) {
        return repository.findById(id).filter(d -> d.getDeleted() == 0).map(SystemConfigConverter::toDomain);
    }

    @Override
    public Optional<SystemConfig> findByConfigKey(String configKey) {
        return repository.findByConfigKeyAndDeleted(configKey, 0).map(SystemConfigConverter::toDomain);
    }

    @Override
    public PageResult<SystemConfig> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderById(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(SystemConfigConverter::toDomain).toList(),
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
