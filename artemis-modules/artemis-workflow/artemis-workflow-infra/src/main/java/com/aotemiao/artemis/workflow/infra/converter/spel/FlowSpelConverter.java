package com.aotemiao.artemis.workflow.infra.converter.spel;

import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import com.aotemiao.artemis.workflow.infra.dataobject.spel.FlowSpelDO;

/** 流程 SpEL 表达式转换器。 */
public final class FlowSpelConverter {

    private FlowSpelConverter() {}

    public static FlowSpel toDomain(FlowSpelDO entity) {
        FlowSpel spel = new FlowSpel();
        spel.setId(entity.getId());
        spel.setComponentName(entity.getComponentName());
        spel.setMethodName(entity.getMethodName());
        spel.setParameters(entity.getParameters());
        spel.setPreviewExpression(entity.getPreviewExpression());
        spel.setRemarks(entity.getRemarks());
        spel.setStatus(entity.getStatus());
        return spel;
    }

    public static FlowSpelDO toDO(FlowSpel spel) {
        FlowSpelDO entity = new FlowSpelDO();
        entity.setId(spel.getId());
        entity.setComponentName(spel.getComponentName());
        entity.setMethodName(spel.getMethodName());
        entity.setParameters(spel.getParameters());
        entity.setPreviewExpression(spel.getPreviewExpression());
        entity.setRemarks(spel.getRemarks());
        entity.setStatus(spel.getStatus());
        return entity;
    }
}
