package com.aotemiao.artemis.workflow.app.command.spel;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.domain.gateway.spel.FlowSpelGateway;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class FakeFlowSpelGateway implements FlowSpelGateway {

    private final List<FlowSpel> spels = new ArrayList<>();
    private long sequence = 1L;

    @Override
    public FlowSpel save(FlowSpel flowSpel) {
        if (flowSpel.getId() == null) {
            flowSpel.setId(sequence++);
        }
        spels.removeIf(existing -> existing.getId().equals(flowSpel.getId()));
        spels.add(copy(flowSpel));
        return copy(flowSpel);
    }

    @Override
    public Optional<FlowSpel> findById(Long id) {
        return spels.stream()
                .filter(spel -> spel.getId().equals(id))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public Optional<FlowSpel> findByPreviewExpression(String previewExpression) {
        return spels.stream()
                .filter(spel -> spel.getPreviewExpression().equals(previewExpression))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public PageResult<FlowSpel> findPage(PageRequest pageRequest) {
        return PageResult.of(spels.size(), findAll(), 1);
    }

    @Override
    public List<FlowSpel> findAll() {
        return spels.stream()
                .sorted(Comparator.comparing(FlowSpel::getId))
                .map(this::copy)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        spels.removeIf(spel -> spel.getId().equals(id));
    }

    private FlowSpel copy(FlowSpel source) {
        FlowSpel target = new FlowSpel();
        target.setId(source.getId());
        target.setComponentName(source.getComponentName());
        target.setMethodName(source.getMethodName());
        target.setParameters(source.getParameters());
        target.setPreviewExpression(source.getPreviewExpression());
        target.setRemarks(source.getRemarks());
        target.setStatus(source.getStatus());
        return target;
    }
}
