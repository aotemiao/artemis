package com.aotemiao.artemis.system.infra.converter.audit;

import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import com.aotemiao.artemis.system.infra.dataobject.audit.LoginInfoDO;

public final class LoginInfoConverter {

    private LoginInfoConverter() {}

    public static LoginInfoDO toDO(LoginInfo domain) {
        LoginInfoDO entity = new LoginInfoDO();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setUsername(domain.getUsername());
        entity.setClientId(domain.getClientId());
        entity.setDeviceType(domain.getDeviceType());
        entity.setIpaddr(domain.getIpaddr());
        entity.setLoginLocation(domain.getLoginLocation());
        entity.setBrowser(domain.getBrowser());
        entity.setOs(domain.getOs());
        entity.setStatus(domain.getStatus());
        entity.setMsg(domain.getMsg());
        entity.setLoginTime(domain.getLoginTime());
        return entity;
    }

    public static LoginInfo toDomain(LoginInfoDO entity) {
        LoginInfo domain = new LoginInfo();
        domain.setId(entity.getId());
        domain.setTenantId(entity.getTenantId());
        domain.setUsername(entity.getUsername());
        domain.setClientId(entity.getClientId());
        domain.setDeviceType(entity.getDeviceType());
        domain.setIpaddr(entity.getIpaddr());
        domain.setLoginLocation(entity.getLoginLocation());
        domain.setBrowser(entity.getBrowser());
        domain.setOs(entity.getOs());
        domain.setStatus(entity.getStatus());
        domain.setMsg(entity.getMsg());
        domain.setLoginTime(entity.getLoginTime());
        return domain;
    }
}
