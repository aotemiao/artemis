package com.aotemiao.artemis.workflow.infra.repository.spel;

import com.aotemiao.artemis.workflow.infra.dataobject.spel.FlowSpelDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface FlowSpelRepository extends CrudRepository<FlowSpelDO, Long> {

    Page<FlowSpelDO> findAllByDeletedOrderByIdDesc(Integer deleted, Pageable pageable);

    List<FlowSpelDO> findAllByDeletedOrderByIdDesc(Integer deleted);

    Optional<FlowSpelDO> findByPreviewExpressionAndDeleted(String previewExpression, Integer deleted);
}
