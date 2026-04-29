package com.aotemiao.artemis.workflow.app.command.category;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.domain.gateway.category.FlowCategoryGateway;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class FakeFlowCategoryGateway implements FlowCategoryGateway {

    private final List<FlowCategory> categories = new ArrayList<>();
    private long sequence = 1L;

    @Override
    public FlowCategory save(FlowCategory flowCategory) {
        if (flowCategory.getId() == null) {
            flowCategory.setId(sequence++);
            categories.add(copy(flowCategory));
            return copy(flowCategory);
        }
        categories.removeIf(existing -> existing.getId().equals(flowCategory.getId()));
        categories.add(copy(flowCategory));
        return copy(flowCategory);
    }

    @Override
    public Optional<FlowCategory> findById(Long id) {
        return categories.stream()
                .filter(category -> category.getId().equals(id))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public Optional<FlowCategory> findByParentIdAndCategoryName(Long parentId, String categoryName) {
        return categories.stream()
                .filter(category -> category.getParentId().equals(parentId))
                .filter(category -> category.getCategoryName().equals(categoryName))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public PageResult<FlowCategory> findPage(PageRequest pageRequest) {
        return PageResult.of(categories.size(), findAll(), 1);
    }

    @Override
    public List<FlowCategory> findAll() {
        return categories.stream()
                .sorted(Comparator.comparing(FlowCategory::getSortOrder).thenComparing(FlowCategory::getId))
                .map(this::copy)
                .toList();
    }

    @Override
    public boolean existsByParentId(Long parentId) {
        return categories.stream().anyMatch(category -> category.getParentId().equals(parentId));
    }

    @Override
    public void deleteById(Long id) {
        categories.removeIf(category -> category.getId().equals(id));
    }

    private FlowCategory copy(FlowCategory source) {
        FlowCategory target = new FlowCategory();
        target.setId(source.getId());
        target.setParentId(source.getParentId());
        target.setAncestors(source.getAncestors());
        target.setCategoryName(source.getCategoryName());
        target.setSortOrder(source.getSortOrder());
        target.setRemarks(source.getRemarks());
        return target;
    }
}
