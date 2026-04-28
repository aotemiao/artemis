package com.aotemiao.artemis.system.infra.converter.tenant;

import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import com.aotemiao.artemis.system.infra.dataobject.tenant.TenantPackageDO;
import java.util.List;

public final class TenantPackageConverter {

    private TenantPackageConverter() {}

    public static TenantPackage toDomain(TenantPackageDO source, List<Long> menuIds) {
        TenantPackage target = new TenantPackage();
        target.setId(source.getId());
        target.setPackageName(source.getPackageName());
        target.setMenuCheckStrictly(Boolean.TRUE.equals(source.getMenuCheckStrictly()));
        target.setEnabled(Boolean.TRUE.equals(source.getEnabled()));
        target.setRemarks(source.getRemarks());
        target.setMenuIds(menuIds);
        return target;
    }

    public static TenantPackageDO toDO(TenantPackage source) {
        TenantPackageDO target = new TenantPackageDO();
        target.setId(source.getId());
        target.setPackageName(source.getPackageName());
        target.setMenuCheckStrictly(source.isMenuCheckStrictly());
        target.setEnabled(source.isEnabled());
        target.setRemarks(source.getRemarks());
        target.setDeleted(0);
        return target;
    }
}
