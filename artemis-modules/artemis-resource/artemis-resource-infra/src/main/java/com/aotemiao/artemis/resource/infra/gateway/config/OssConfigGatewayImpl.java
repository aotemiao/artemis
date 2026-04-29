package com.aotemiao.artemis.resource.infra.gateway.config;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.resource.domain.gateway.config.OssConfigGateway;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import com.aotemiao.artemis.resource.infra.converter.config.OssConfigConverter;
import com.aotemiao.artemis.resource.infra.repository.config.OssConfigRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OssConfigGatewayImpl implements OssConfigGateway {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects repository as a managed collaborator; this gateway does not expose it.")
    private final OssConfigRepository repository;

    public OssConfigGatewayImpl(OssConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OssConfig save(OssConfig ossConfig) {
        return OssConfigConverter.toDomain(repository.save(OssConfigConverter.toDO(ossConfig)));
    }

    @Override
    public Optional<OssConfig> findById(Long id) {
        return repository
                .findById(id)
                .filter(entity -> Integer.valueOf(0).equals(entity.getDeleted()))
                .map(OssConfigConverter::toDomain);
    }

    @Override
    public Optional<OssConfig> findByConfigKey(String configKey) {
        return repository.findByConfigKeyAndDeleted(configKey, 0).map(OssConfigConverter::toDomain);
    }

    @Override
    public PageResult<OssConfig> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderByIdDesc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(OssConfigConverter::toDomain).toList(),
                pr.totalPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeleted(1);
            entity.setDefaultFlag(0);
            repository.save(entity);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearDefaultExcept(Long id) {
        repository.findAllByDefaultFlagAndDeleted(1, 0).stream()
                .filter(entity -> !entity.getId().equals(id))
                .forEach(entity -> {
                    entity.setDefaultFlag(0);
                    repository.save(entity);
                });
    }
}
