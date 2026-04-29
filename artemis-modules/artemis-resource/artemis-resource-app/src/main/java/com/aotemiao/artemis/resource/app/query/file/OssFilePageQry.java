package com.aotemiao.artemis.resource.app.query.file;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import java.io.Serializable;

/** OSS 文件分页查询。 */
public record OssFilePageQry(PageRequest pageRequest) implements Serializable {}
