package com.aotemiao.artemis.workflow.infra.gateway.spel;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.workflow.domain.gateway.spel.FlowSpelGateway;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import com.aotemiao.artemis.workflow.infra.converter.spel.FlowSpelConverter;
import com.aotemiao.artemis.workflow.infra.repository.spel.FlowSpelRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FlowSpelGatewayImpl implements FlowSpelGateway {

    private final FlowSpelRepository repository;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects repository as a managed collaborator; this gateway does not expose it.")
    public FlowSpelGatewayImpl(FlowSpelRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowSpel save(FlowSpel flowSpel) {
        return FlowSpelConverter.toDomain(repository.save(FlowSpelConverter.toDO(flowSpel)));
    }

    @Override
    public Optional<FlowSpel> findById(Long id) {
        return repository
                .findById(id)
                .filter(entity -> Integer.valueOf(0).equals(entity.getDeleted()))
                .map(FlowSpelConverter::toDomain);
    }

    @Override
    public Optional<FlowSpel> findByPreviewExpression(String previewExpression) {
        return repository
                .findByPreviewExpressionAndDeleted(previewExpression, 0)
                .map(FlowSpelConverter::toDomain);
    }

    @Override
    public PageResult<FlowSpel> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderByIdDesc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(FlowSpelConverter::toDomain).toList(),
                pr.totalPages());
    }

    @Override
    public List<FlowSpel> findAll() {
        return repository.findAllByDeletedOrderByIdDesc(0).stream()
                .map(FlowSpelConverter::toDomain)
                .toList();
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
