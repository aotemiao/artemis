package com.aotemiao.artemis.system.infra.repository.post;

import com.aotemiao.artemis.system.infra.dataobject.post.SystemUserPostDO;
import org.springframework.data.repository.CrudRepository;

public interface SystemUserPostRepository extends CrudRepository<SystemUserPostDO, Long> {

    long countByPostId(Long postId);
}
