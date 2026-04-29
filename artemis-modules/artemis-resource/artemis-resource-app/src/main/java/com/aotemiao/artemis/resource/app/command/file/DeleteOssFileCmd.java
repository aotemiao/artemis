package com.aotemiao.artemis.resource.app.command.file;

import java.io.Serializable;

/** 删除 OSS 文件命令。 */
public record DeleteOssFileCmd(Long id) implements Serializable {}
