package com.aotemiao.artemis.workflow.infra.converter.definition;

import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import com.aotemiao.artemis.workflow.infra.dataobject.definition.FlowDefinitionDO;

/** 流程定义转换器。 */
public final class FlowDefinitionConverter {

    private FlowDefinitionConverter() {}

    public static FlowDefinition toDomain(FlowDefinitionDO entity) {
        FlowDefinition definition = new FlowDefinition();
        definition.setId(entity.getId());
        definition.setFlowCode(entity.getFlowCode());
        definition.setFlowName(entity.getFlowName());
        definition.setModelType(entity.getModelType());
        definition.setCategoryId(entity.getCategoryId());
        definition.setVersion(entity.getVersion());
        definition.setPublishStatus(entity.getPublishStatus());
        definition.setCustomForm(entity.getCustomForm());
        definition.setFormPath(entity.getFormPath());
        definition.setActiveStatus(entity.getActiveStatus());
        definition.setListener(entity.getListener());
        definition.setExtJson(entity.getExtJson());
        definition.setTenantId(entity.getTenantId());
        definition.setDefinitionJson(entity.getDefinitionJson());
        definition.setDefinitionXml(entity.getDefinitionXml());
        return definition;
    }

    public static FlowDefinitionDO toDO(FlowDefinition definition) {
        FlowDefinitionDO entity = new FlowDefinitionDO();
        entity.setId(definition.getId());
        entity.setFlowCode(definition.getFlowCode());
        entity.setFlowName(definition.getFlowName());
        entity.setModelType(definition.getModelType());
        entity.setCategoryId(definition.getCategoryId());
        entity.setVersion(definition.getVersion());
        entity.setPublishStatus(definition.getPublishStatus());
        entity.setCustomForm(definition.getCustomForm());
        entity.setFormPath(definition.getFormPath());
        entity.setActiveStatus(definition.getActiveStatus());
        entity.setListener(definition.getListener());
        entity.setExtJson(definition.getExtJson());
        entity.setTenantId(definition.getTenantId());
        entity.setDefinitionJson(definition.getDefinitionJson());
        entity.setDefinitionXml(definition.getDefinitionXml());
        return entity;
    }
}
