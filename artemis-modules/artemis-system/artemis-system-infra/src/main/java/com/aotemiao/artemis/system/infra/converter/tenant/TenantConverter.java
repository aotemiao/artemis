package com.aotemiao.artemis.system.infra.converter.tenant;

import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import com.aotemiao.artemis.system.infra.dataobject.tenant.TenantDO;

public final class TenantConverter {

    private TenantConverter() {}

    public static Tenant toDomain(TenantDO source) {
        Tenant target = new Tenant();
        target.setId(source.getId());
        target.setTenantNo(source.getTenantNo());
        target.setCompanyName(source.getCompanyName());
        target.setContactName(source.getContactName());
        target.setContactPhone(source.getContactPhone());
        target.setSocialCreditCode(source.getSocialCreditCode());
        target.setAddress(source.getAddress());
        target.setDomain(source.getDomain());
        target.setIntro(source.getIntro());
        target.setPackageId(source.getPackageId());
        target.setExpireTime(source.getExpireTime());
        target.setUserLimit(source.getUserLimit());
        target.setStatus(source.getStatus());
        target.setRemarks(source.getRemarks());
        return target;
    }

    public static TenantDO toDO(Tenant source) {
        TenantDO target = new TenantDO();
        target.setId(source.getId());
        target.setTenantNo(source.getTenantNo());
        target.setCompanyName(source.getCompanyName());
        target.setContactName(source.getContactName());
        target.setContactPhone(source.getContactPhone());
        target.setSocialCreditCode(source.getSocialCreditCode());
        target.setAddress(source.getAddress());
        target.setDomain(source.getDomain());
        target.setIntro(source.getIntro());
        target.setPackageId(source.getPackageId());
        target.setExpireTime(source.getExpireTime());
        target.setUserLimit(source.getUserLimit());
        target.setStatus(source.getStatus());
        target.setRemarks(source.getRemarks());
        target.setDeleted(0);
        return target;
    }
}
