package com.aotemiao.artemis.system.infra.converter.post;

import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import com.aotemiao.artemis.system.infra.dataobject.post.SystemPostDO;

public final class SystemPostConverter {

    private SystemPostConverter() {}

    public static SystemPost toDomain(SystemPostDO source) {
        SystemPost target = new SystemPost();
        target.setId(source.getId());
        target.setDeptId(source.getDeptId());
        target.setPostCode(source.getPostCode());
        target.setPostCategory(source.getPostCategory());
        target.setPostName(source.getPostName());
        target.setSortOrder(source.getSortOrder());
        target.setStatus(source.getStatus());
        target.setRemarks(source.getRemarks());
        return target;
    }

    public static SystemPostDO toDO(SystemPost source) {
        SystemPostDO target = new SystemPostDO();
        target.setId(source.getId());
        target.setDeptId(source.getDeptId());
        target.setPostCode(source.getPostCode());
        target.setPostCategory(source.getPostCategory());
        target.setPostName(source.getPostName());
        target.setSortOrder(source.getSortOrder());
        target.setStatus(source.getStatus());
        target.setRemarks(source.getRemarks());
        target.setDeleted(0);
        return target;
    }
}
