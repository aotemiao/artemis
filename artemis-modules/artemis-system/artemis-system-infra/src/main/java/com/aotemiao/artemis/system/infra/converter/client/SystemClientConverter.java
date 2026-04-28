package com.aotemiao.artemis.system.infra.converter.client;

import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import com.aotemiao.artemis.system.infra.dataobject.client.SystemClientDO;

public final class SystemClientConverter {

    private SystemClientConverter() {}

    public static SystemClient toDomain(SystemClientDO source) {
        SystemClient target = new SystemClient();
        target.setId(source.getId());
        target.setClientId(source.getClientId());
        target.setClientKey(source.getClientKey());
        target.setClientSecret(source.getClientSecret());
        target.setGrantTypes(source.getGrantTypes());
        target.setDeviceType(source.getDeviceType());
        target.setActiveTimeoutSeconds(source.getActiveTimeoutSeconds());
        target.setFixedTimeoutSeconds(source.getFixedTimeoutSeconds());
        target.setStatus(source.getStatus());
        target.setRemarks(source.getRemarks());
        return target;
    }

    public static SystemClientDO toDO(SystemClient source) {
        SystemClientDO target = new SystemClientDO();
        target.setId(source.getId());
        target.setClientId(source.getClientId());
        target.setClientKey(source.getClientKey());
        target.setClientSecret(source.getClientSecret());
        target.setGrantTypes(source.getGrantTypes());
        target.setDeviceType(source.getDeviceType());
        target.setActiveTimeoutSeconds(source.getActiveTimeoutSeconds());
        target.setFixedTimeoutSeconds(source.getFixedTimeoutSeconds());
        target.setStatus(source.getStatus());
        target.setRemarks(source.getRemarks());
        target.setDeleted(0);
        return target;
    }
}
