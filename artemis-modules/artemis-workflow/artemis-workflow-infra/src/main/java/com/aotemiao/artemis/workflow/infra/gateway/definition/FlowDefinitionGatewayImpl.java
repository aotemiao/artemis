package com.aotemiao.artemis.workflow.infra.gateway.definition;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import com.aotemiao.artemis.workflow.infra.converter.definition.FlowDefinitionConverter;
import com.aotemiao.artemis.workflow.infra.repository.definition.FlowDefinitionRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FlowDefinitionGatewayImpl implements FlowDefinitionGateway {

    private static final int UNPUBLISHED = 0;

    private final FlowDefinitionRepository repository;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects repository as a managed collaborator; this gateway does not expose it.")
    public FlowDefinitionGatewayImpl(FlowDefinitionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowDefinition save(FlowDefinition definition) {
        return FlowDefinitionConverter.toDomain(repository.save(FlowDefinitionConverter.toDO(definition)));
    }

    @Override
    public Optional<FlowDefinition> findById(Long id) {
        return repository
                .findById(id)
                .filter(entity -> Integer.valueOf(0).equals(entity.getDeleted()))
                .map(FlowDefinitionConverter::toDomain);
    }

    @Override
    public Optional<FlowDefinition> findByFlowCodeAndTenantId(String flowCode, String tenantId) {
        return repository
                .findByFlowCodeAndTenantIdAndDeleted(flowCode, tenantId, 0)
                .map(FlowDefinitionConverter::toDomain);
    }

    @Override
    public PageResult<FlowDefinition> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderByIdDesc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(FlowDefinitionConverter::toDomain).toList(),
                pr.totalPages());
    }

    @Override
    public List<FlowDefinition> findAll() {
        return repository.findAllByDeletedOrderByIdDesc(0).stream()
                .map(FlowDefinitionConverter::toDomain)
                .toList();
    }

    @Override
    public List<FlowDefinition> findUnpublished() {
        return repository.findAllByPublishStatusAndDeletedOrderByIdDesc(UNPUBLISHED, 0).stream()
                .map(FlowDefinitionConverter::toDomain)
                .toList();
    }

    @Override
    public boolean existsUsedByInstance(Long id) {
        return false;
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
