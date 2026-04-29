package com.aotemiao.artemis.workflow.infra.gateway.category;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.workflow.domain.gateway.category.FlowCategoryGateway;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import com.aotemiao.artemis.workflow.infra.converter.category.FlowCategoryConverter;
import com.aotemiao.artemis.workflow.infra.repository.category.FlowCategoryRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FlowCategoryGatewayImpl implements FlowCategoryGateway {

    private final FlowCategoryRepository repository;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects repository as a managed collaborator; this gateway does not expose it.")
    public FlowCategoryGatewayImpl(FlowCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowCategory save(FlowCategory flowCategory) {
        return FlowCategoryConverter.toDomain(repository.save(FlowCategoryConverter.toDO(flowCategory)));
    }

    @Override
    public Optional<FlowCategory> findById(Long id) {
        return repository
                .findById(id)
                .filter(entity -> Integer.valueOf(0).equals(entity.getDeleted()))
                .map(FlowCategoryConverter::toDomain);
    }

    @Override
    public Optional<FlowCategory> findByParentIdAndCategoryName(Long parentId, String categoryName) {
        return repository
                .findByParentIdAndCategoryNameAndDeleted(parentId, categoryName, 0)
                .map(FlowCategoryConverter::toDomain);
    }

    @Override
    public PageResult<FlowCategory> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderByIdDesc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(FlowCategoryConverter::toDomain).toList(),
                pr.totalPages());
    }

    @Override
    public List<FlowCategory> findAll() {
        return repository.findAllByDeletedOrderBySortOrderAscIdAsc(0).stream()
                .map(FlowCategoryConverter::toDomain)
                .toList();
    }

    @Override
    public boolean existsByParentId(Long parentId) {
        return repository.existsByParentIdAndDeleted(parentId, 0);
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
