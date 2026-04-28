package com.aotemiao.artemis.system.infra.gateway.audit;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.audit.OperLogGateway;
import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import com.aotemiao.artemis.system.infra.converter.audit.OperLogConverter;
import com.aotemiao.artemis.system.infra.dataobject.audit.OperLogDO;
import com.aotemiao.artemis.system.infra.repository.audit.OperLogRepository;
import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OperLogGatewayImpl implements OperLogGateway {

    private final OperLogRepository repository;

    public OperLogGatewayImpl(OperLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperLog save(OperLog operLog) {
        return OperLogConverter.toDomain(repository.save(OperLogConverter.toDO(operLog)));
    }

    @Override
    public Optional<OperLog> findById(Long id) {
        return repository.findById(id).filter(d -> d.getDeleted() == 0).map(OperLogConverter::toDomain);
    }

    @Override
    public PageResult<OperLog> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderByOperTimeDesc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(OperLogConverter::toDomain).toList(),
                pr.totalPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Collection<Long> ids) {
        for (Long id : ids) {
            repository.findById(id).ifPresent(entity -> {
                entity.setDeleted(1);
                repository.save(entity);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear() {
        for (OperLogDO entity : repository.findAllByDeleted(0)) {
            entity.setDeleted(1);
            repository.save(entity);
        }
    }
}
