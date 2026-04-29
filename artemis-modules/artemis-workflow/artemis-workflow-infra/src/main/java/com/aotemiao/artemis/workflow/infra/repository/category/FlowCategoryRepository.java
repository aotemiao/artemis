package com.aotemiao.artemis.workflow.infra.repository.category;

import com.aotemiao.artemis.workflow.infra.dataobject.category.FlowCategoryDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface FlowCategoryRepository extends CrudRepository<FlowCategoryDO, Long> {

    Page<FlowCategoryDO> findAllByDeletedOrderByIdDesc(Integer deleted, Pageable pageable);

    List<FlowCategoryDO> findAllByDeletedOrderBySortOrderAscIdAsc(Integer deleted);

    Optional<FlowCategoryDO> findByParentIdAndCategoryNameAndDeleted(
            Long parentId, String categoryName, Integer deleted);

    boolean existsByParentIdAndDeleted(Long parentId, Integer deleted);
}
