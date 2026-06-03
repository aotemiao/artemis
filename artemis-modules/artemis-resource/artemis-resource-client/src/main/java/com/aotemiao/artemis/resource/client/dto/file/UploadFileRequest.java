package com.aotemiao.artemis.resource.client.dto.file;

import java.io.Serializable;
import java.util.Arrays;

/** 远程上传文件请求。 */
public record UploadFileRequest(String originalFileName, byte[] content, String uploader, String extJson)
        implements Serializable {

    public UploadFileRequest {
        content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }
}
