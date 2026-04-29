package com.aotemiao.artemis.workflow.domain.gateway.definition;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import java.util.List;
import java.util.Optional;

/** 流程定义 Gateway。 */
public interface FlowDefinitionGateway {

    FlowDefinition save(FlowDefinition definition);

    Optional<FlowDefinition> findById(Long id);

    Optional<FlowDefinition> findByFlowCodeAndTenantId(String flowCode, String tenantId);

    PageResult<FlowDefinition> findPage(PageRequest pageRequest);

    List<FlowDefinition> findAll();

    List<FlowDefinition> findUnpublished();

    boolean existsUsedByInstance(Long id);

    void deleteById(Long id);
}
