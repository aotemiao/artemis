package com.aotemiao.artemis.system.app.query.department;

import java.io.Serializable;

/** 查询系统部门列表。 */
public record ListSystemDepartmentQry(Long excludeId) implements Serializable {}
