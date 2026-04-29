package com.aotemiao.artemis.workflow.domain.gateway.spel;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import java.util.List;
import java.util.Optional;

/** 流程 SpEL 表达式 Gateway。 */
public interface FlowSpelGateway {

    FlowSpel save(FlowSpel flowSpel);

    Optional<FlowSpel> findById(Long id);

    Optional<FlowSpel> findByPreviewExpression(String previewExpression);

    PageResult<FlowSpel> findPage(PageRequest pageRequest);

    List<FlowSpel> findAll();

    void deleteById(Long id);
}
