package com.aotemiao.artemis.workflow.domain.gateway.category;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import java.util.List;
import java.util.Optional;

/** 流程分类 Gateway。 */
public interface FlowCategoryGateway {

    FlowCategory save(FlowCategory flowCategory);

    Optional<FlowCategory> findById(Long id);

    Optional<FlowCategory> findByParentIdAndCategoryName(Long parentId, String categoryName);

    PageResult<FlowCategory> findPage(PageRequest pageRequest);

    List<FlowCategory> findAll();

    boolean existsByParentId(Long parentId);

    void deleteById(Long id);
}
