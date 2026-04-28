package com.aotemiao.artemis.system.app.command.department;

import java.io.Serializable;

/** 删除系统部门命令。 */
public record DeleteSystemDepartmentCmd(Long id) implements Serializable {}
