package com.aotemiao.artemis.resource.domain.model.file;

import java.io.Serializable;

/** 已写入对象存储的文件对象。 */
public record StoredObject(String fileName, String suffix, String url, String objectKey, long sizeBytes)
        implements Serializable {}
