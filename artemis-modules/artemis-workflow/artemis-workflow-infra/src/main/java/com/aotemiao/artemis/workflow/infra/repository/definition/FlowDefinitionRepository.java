package com.aotemiao.artemis.workflow.infra.repository.definition;

import com.aotemiao.artemis.workflow.infra.dataobject.definition.FlowDefinitionDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface FlowDefinitionRepository extends CrudRepository<FlowDefinitionDO, Long> {

    Page<FlowDefinitionDO> findAllByDeletedOrderByIdDesc(Integer deleted, Pageable pageable);

    List<FlowDefinitionDO> findAllByDeletedOrderByIdDesc(Integer deleted);

    List<FlowDefinitionDO> findAllByPublishStatusAndDeletedOrderByIdDesc(Integer publishStatus, Integer deleted);

    Optional<FlowDefinitionDO> findByFlowCodeAndTenantIdAndDeleted(String flowCode, String tenantId, Integer deleted);
}
