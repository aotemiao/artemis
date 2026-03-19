package com.aotemiao.artemis.system.domain.gateway;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import com.aotemiao.artemis.system.domain.model.LookupType;
import java.util.List;
import java.util.Optional;

/** 字典类型聚合及按类型查询字典项的 Gateway。 */
public interface LookupTypeGateway {

    LookupType save(LookupType lookupType);

    Optional<LookupType> findById(Long id);

    PageResult<LookupType> findPage(PageRequest pageRequest);

    void deleteById(Long id);

    List<LookupItem> findItemsByTypeCode(String typeCode);
}
