package com.aotemiao.artemis.workflow.app.command.definition;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class FakeFlowDefinitionGateway implements FlowDefinitionGateway {

    private final List<FlowDefinition> definitions = new ArrayList<>();
    private long sequence = 1L;
    private boolean usedByInstance;

    @Override
    public FlowDefinition save(FlowDefinition definition) {
        if (definition.getId() == null) {
            definition.setId(sequence++);
        }
        definitions.removeIf(existing -> existing.getId().equals(definition.getId()));
        definitions.add(copy(definition));
        return copy(definition);
    }

    @Override
    public Optional<FlowDefinition> findById(Long id) {
        return definitions.stream()
                .filter(definition -> definition.getId().equals(id))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public Optional<FlowDefinition> findByFlowCodeAndTenantId(String flowCode, String tenantId) {
        return definitions.stream()
                .filter(definition -> definition.getFlowCode().equals(flowCode))
                .filter(definition -> definition.getTenantId().equals(tenantId))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public PageResult<FlowDefinition> findPage(PageRequest pageRequest) {
        return PageResult.of(definitions.size(), findAll(), 1);
    }

    @Override
    public List<FlowDefinition> findAll() {
        return definitions.stream()
                .sorted(Comparator.comparing(FlowDefinition::getId))
                .map(this::copy)
                .toList();
    }

    @Override
    public List<FlowDefinition> findUnpublished() {
        return definitions.stream()
                .filter(definition -> Integer.valueOf(0).equals(definition.getPublishStatus()))
                .map(this::copy)
                .toList();
    }

    @Override
    public boolean existsUsedByInstance(Long id) {
        return usedByInstance;
    }

    @Override
    public void deleteById(Long id) {
        definitions.removeIf(definition -> definition.getId().equals(id));
    }

    void markUsedByInstance() {
        usedByInstance = true;
    }

    private FlowDefinition copy(FlowDefinition source) {
        FlowDefinition target = new FlowDefinition();
        target.setId(source.getId());
        target.setFlowCode(source.getFlowCode());
        target.setFlowName(source.getFlowName());
        target.setModelType(source.getModelType());
        target.setCategoryId(source.getCategoryId());
        target.setVersion(source.getVersion());
        target.setPublishStatus(source.getPublishStatus());
        target.setCustomForm(source.getCustomForm());
        target.setFormPath(source.getFormPath());
        target.setActiveStatus(source.getActiveStatus());
        target.setListener(source.getListener());
        target.setExtJson(source.getExtJson());
        target.setTenantId(source.getTenantId());
        target.setDefinitionJson(source.getDefinitionJson());
        target.setDefinitionXml(source.getDefinitionXml());
        return target;
    }
}
