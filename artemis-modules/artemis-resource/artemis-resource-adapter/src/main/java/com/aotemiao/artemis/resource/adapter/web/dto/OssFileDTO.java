package com.aotemiao.artemis.resource.adapter.web.dto;

import java.io.Serializable;

/** OSS 文件响应 DTO。 */
public record OssFileDTO(
        Long id,
        String fileName,
        String originalFileName,
        String suffix,
        String url,
        String uploader,
        String provider,
        Long sizeBytes,
        String extJson)
        implements Serializable {}
