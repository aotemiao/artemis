package com.aotemiao.artemis.system.infra.converter;

import com.aotemiao.artemis.system.domain.model.LookupItem;
import com.aotemiao.artemis.system.domain.model.LookupType;
import com.aotemiao.artemis.system.infra.dataobject.LookupItemDO;
import com.aotemiao.artemis.system.infra.dataobject.LookupTypeDO;

import java.util.List;

public final class LookupConverter {

    private LookupConverter() {}

    public static LookupType toDomain(LookupTypeDO d) {
        if (d == null) return null;
        LookupType t = new LookupType();
        t.setId(d.getId());
        t.setCode(d.getCode());
        t.setName(d.getName());
        t.setDescription(d.getDescription());
        if (d.getItems() != null) {
            t.setItems(d.getItems().stream().map(LookupConverter::toItemDomain).toList());
        }
        return t;
    }

    public static LookupItem toItemDomain(LookupItemDO d) {
        if (d == null) return null;
        LookupItem i = new LookupItem();
        i.setId(d.getId());
        i.setLookupTypeId(d.getLookupTypeId());
        i.setValue(d.getValue());
        i.setLabel(d.getLabel());
        i.setSortOrder(d.getSortOrder());
        return i;
    }

    public static LookupTypeDO toDO(LookupType t) {
        if (t == null) return null;
        LookupTypeDO d = new LookupTypeDO();
        d.setId(t.getId());
        d.setCode(t.getCode());
        d.setName(t.getName());
        d.setDescription(t.getDescription());
        if (t.getItems() != null) {
            d.setItems(t.getItems().stream()
                    .map(item -> toItemDO(item, t.getId()))
                    .toList());
        }
        return d;
    }

    public static LookupItemDO toItemDO(LookupItem i, Long lookupTypeId) {
        if (i == null) return null;
        LookupItemDO d = new LookupItemDO();
        d.setId(i.getId());
        d.setLookupTypeId(lookupTypeId != null ? lookupTypeId : i.getLookupTypeId());
        d.setValue(i.getValue());
        d.setLabel(i.getLabel());
        d.setSortOrder(i.getSortOrder() != null ? i.getSortOrder() : 0);
        return d;
    }
}
