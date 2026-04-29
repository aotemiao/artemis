package com.aotemiao.artemis.resource.app.command.file;

import java.io.Serializable;
import java.util.Arrays;

/** 上传 OSS 文件命令。 */
public record UploadOssFileCmd(String originalFileName, byte[] content, String uploader, String extJson)
        implements Serializable {

    public UploadOssFileCmd {
        content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }
}
