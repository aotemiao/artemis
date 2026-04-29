package com.aotemiao.artemis.resource.app.query.file;

import java.io.Serializable;

/** 按 ID 查询 OSS 文件。 */
public record FindOssFileByIdQry(Long id) implements Serializable {}
