package com.aotemiao.artemis.resource.client.dto.file;

import java.io.Serializable;

/** 上传文件响应。 */
public record UploadedFileResponse(Long id, String fileName, String originalFileName, String url, String provider)
        implements Serializable {}
