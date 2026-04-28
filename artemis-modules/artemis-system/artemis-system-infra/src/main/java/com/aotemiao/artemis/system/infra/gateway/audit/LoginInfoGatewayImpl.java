package com.aotemiao.artemis.system.infra.gateway.audit;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.audit.LoginInfoGateway;
import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import com.aotemiao.artemis.system.infra.converter.audit.LoginInfoConverter;
import com.aotemiao.artemis.system.infra.dataobject.audit.LoginInfoDO;
import com.aotemiao.artemis.system.infra.repository.audit.LoginInfoRepository;
import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoginInfoGatewayImpl implements LoginInfoGateway {

    private final LoginInfoRepository repository;

    public LoginInfoGatewayImpl(LoginInfoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginInfo save(LoginInfo loginInfo) {
        return LoginInfoConverter.toDomain(repository.save(LoginInfoConverter.toDO(loginInfo)));
    }

    @Override
    public Optional<LoginInfo> findById(Long id) {
        return repository.findById(id).filter(d -> d.getDeleted() == 0).map(LoginInfoConverter::toDomain);
    }

    @Override
    public PageResult<LoginInfo> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderByLoginTimeDesc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(LoginInfoConverter::toDomain).toList(),
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
        for (LoginInfoDO entity : repository.findAllByDeleted(0)) {
            entity.setDeleted(1);
            repository.save(entity);
        }
    }
}
