package com.aotemiao.artemis.system.infra.gateway.notice;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import com.aotemiao.artemis.system.infra.converter.notice.SystemNoticeConverter;
import com.aotemiao.artemis.system.infra.repository.notice.SystemNoticeRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemNoticeGatewayImpl implements SystemNoticeGateway {

    private final SystemNoticeRepository repository;

    public SystemNoticeGatewayImpl(SystemNoticeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemNotice save(SystemNotice systemNotice) {
        return SystemNoticeConverter.toDomain(repository.save(SystemNoticeConverter.toDO(systemNotice)));
    }

    @Override
    public Optional<SystemNotice> findById(Long id) {
        return repository.findById(id).filter(d -> d.getDeleted() == 0).map(SystemNoticeConverter::toDomain);
    }

    @Override
    public PageResult<SystemNotice> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderById(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(SystemNoticeConverter::toDomain).toList(),
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
