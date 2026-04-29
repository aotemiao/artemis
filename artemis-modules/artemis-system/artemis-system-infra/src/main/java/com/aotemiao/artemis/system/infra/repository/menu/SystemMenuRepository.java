package com.aotemiao.artemis.system.infra.repository.menu;

import com.aotemiao.artemis.system.infra.dataobject.menu.SystemMenuDO;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface SystemMenuRepository extends CrudRepository<SystemMenuDO, Long> {

    List<SystemMenuDO> findAllByDeletedOrderByParentIdAscSortOrderAscIdAsc(Integer deleted);

    List<SystemMenuDO> findAllByIdInAndDeletedOrderByParentIdAscSortOrderAscIdAsc(
            Collection<Long> ids, Integer deleted);

    Optional<SystemMenuDO> findByParentIdAndMenuNameAndDeleted(Long parentId, String menuName, Integer deleted);

    Optional<SystemMenuDO> findByPathAndDeleted(String path, Integer deleted);

    List<SystemMenuDO> findAllByParentIdInAndDeleted(Collection<Long> parentIds, Integer deleted);
}
