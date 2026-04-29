package com.aotemiao.artemis.resource.app.query.file;

import java.io.Serializable;
import java.util.Arrays;

/** 下载文件响应。 */
public record DownloadedOssFile(String fileName, byte[] content) implements Serializable {

    public DownloadedOssFile {
        content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }
}
