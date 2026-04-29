package com.aotemiao.artemis.resource.app.query.file;

import java.io.Serializable;
import java.util.List;

/** 按 ID 列表查询 OSS 文件。 */
public record ListOssFilesByIdsQry(List<Long> ids) implements Serializable {

    public ListOssFilesByIdsQry {
        ids = ids == null ? List.of() : List.copyOf(ids);
    }
}
