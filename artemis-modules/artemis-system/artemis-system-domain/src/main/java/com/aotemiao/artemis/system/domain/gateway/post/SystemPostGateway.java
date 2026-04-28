package com.aotemiao.artemis.system.domain.gateway.post;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import java.util.List;
import java.util.Optional;

/** 系统岗位 Gateway。 */
public interface SystemPostGateway {

    SystemPost save(SystemPost systemPost);

    Optional<SystemPost> findById(Long id);

    Optional<SystemPost> findByPostCode(String postCode);

    Optional<SystemPost> findByDeptIdAndPostName(Long deptId, String postName);

    PageResult<SystemPost> findPage(PageRequest pageRequest);

    List<SystemPost> findAll();

    long countUsersByPostId(Long postId);

    void deleteById(Long id);
}
