package com.aotemiao.artemis.workflow.infra.converter.category;

import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import com.aotemiao.artemis.workflow.infra.dataobject.category.FlowCategoryDO;

/** 流程分类转换器。 */
public final class FlowCategoryConverter {

    private FlowCategoryConverter() {}

    public static FlowCategory toDomain(FlowCategoryDO entity) {
        FlowCategory category = new FlowCategory();
        category.setId(entity.getId());
        category.setParentId(entity.getParentId());
        category.setAncestors(entity.getAncestors());
        category.setCategoryName(entity.getCategoryName());
        category.setSortOrder(entity.getSortOrder());
        category.setRemarks(entity.getRemarks());
        return category;
    }

    public static FlowCategoryDO toDO(FlowCategory category) {
        FlowCategoryDO entity = new FlowCategoryDO();
        entity.setId(category.getId());
        entity.setParentId(category.getParentId());
        entity.setAncestors(category.getAncestors());
        entity.setCategoryName(category.getCategoryName());
        entity.setSortOrder(category.getSortOrder());
        entity.setRemarks(category.getRemarks());
        return entity;
    }
}
