package com.aotemiao.artemis.workflow.infra.gateway.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import com.aotemiao.artemis.workflow.infra.converter.category.FlowCategoryConverter;
import com.aotemiao.artemis.workflow.infra.dataobject.category.FlowCategoryDO;
import org.junit.jupiter.api.Test;

class FlowCategoryGatewayImplTest {

    @Test
    void converter_should_round_trip_category_fields() {
        FlowCategory category = new FlowCategory();
        category.setId(1L);
        category.setParentId(0L);
        category.setAncestors("0");
        category.setCategoryName("审批流程");
        category.setSortOrder(1);
        category.setRemarks("remarks");

        FlowCategoryDO entity = FlowCategoryConverter.toDO(category);
        FlowCategory converted = FlowCategoryConverter.toDomain(entity);

        assertThat(converted.getId()).isEqualTo(1L);
        assertThat(converted.getParentId()).isZero();
        assertThat(converted.getAncestors()).isEqualTo("0");
        assertThat(converted.getCategoryName()).isEqualTo("审批流程");
        assertThat(converted.getSortOrder()).isEqualTo(1);
        assertThat(converted.getRemarks()).isEqualTo("remarks");
    }
}
