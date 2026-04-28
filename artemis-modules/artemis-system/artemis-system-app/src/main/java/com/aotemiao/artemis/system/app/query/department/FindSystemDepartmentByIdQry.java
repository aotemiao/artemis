package com.aotemiao.artemis.system.app.query.department;

import java.io.Serializable;

/** 按 ID 查询系统部门。 */
public record FindSystemDepartmentByIdQry(Long id) implements Serializable {}
