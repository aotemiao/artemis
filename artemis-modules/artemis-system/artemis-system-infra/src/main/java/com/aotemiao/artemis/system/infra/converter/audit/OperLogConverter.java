package com.aotemiao.artemis.system.infra.converter.audit;

import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import com.aotemiao.artemis.system.infra.dataobject.audit.OperLogDO;

public final class OperLogConverter {

    private OperLogConverter() {}

    public static OperLogDO toDO(OperLog domain) {
        OperLogDO entity = new OperLogDO();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setBusinessType(domain.getBusinessType());
        entity.setMethod(domain.getMethod());
        entity.setRequestMethod(domain.getRequestMethod());
        entity.setOperatorType(domain.getOperatorType());
        entity.setOperName(domain.getOperName());
        entity.setDeptName(domain.getDeptName());
        entity.setOperUrl(domain.getOperUrl());
        entity.setOperIp(domain.getOperIp());
        entity.setOperLocation(domain.getOperLocation());
        entity.setOperParam(domain.getOperParam());
        entity.setJsonResult(domain.getJsonResult());
        entity.setStatus(domain.getStatus());
        entity.setErrorMsg(domain.getErrorMsg());
        entity.setCostTime(domain.getCostTime());
        entity.setOperTime(domain.getOperTime());
        return entity;
    }

    public static OperLog toDomain(OperLogDO entity) {
        OperLog domain = new OperLog();
        domain.setId(entity.getId());
        domain.setTitle(entity.getTitle());
        domain.setBusinessType(entity.getBusinessType());
        domain.setMethod(entity.getMethod());
        domain.setRequestMethod(entity.getRequestMethod());
        domain.setOperatorType(entity.getOperatorType());
        domain.setOperName(entity.getOperName());
        domain.setDeptName(entity.getDeptName());
        domain.setOperUrl(entity.getOperUrl());
        domain.setOperIp(entity.getOperIp());
        domain.setOperLocation(entity.getOperLocation());
        domain.setOperParam(entity.getOperParam());
        domain.setJsonResult(entity.getJsonResult());
        domain.setStatus(entity.getStatus());
        domain.setErrorMsg(entity.getErrorMsg());
        domain.setCostTime(entity.getCostTime());
        domain.setOperTime(entity.getOperTime());
        return domain;
    }
}
