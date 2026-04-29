package com.aotemiao.artemis.resource.client.dto;

import java.io.Serializable;

/** 文件 URL 响应。 */
public record FileUrlResponse(Long id, String url) implements Serializable {}
