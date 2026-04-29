package com.aotemiao.artemis.resource.domain.model.file;

import java.io.Serializable;
import java.util.Arrays;

/** 从对象存储读取的文件对象。 */
public record DownloadedObject(String fileName, byte[] content) implements Serializable {

    public DownloadedObject {
        content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }
}
