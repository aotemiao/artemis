package com.aotemiao.artemis.resource.domain.gateway.file;

import com.aotemiao.artemis.resource.domain.model.file.DownloadedObject;
import com.aotemiao.artemis.resource.domain.model.file.StoredObject;

/** 对象存储 Gateway。 */
public interface ObjectStorageGateway {

    StoredObject store(String originalFileName, byte[] content);

    DownloadedObject load(String objectKey);

    void delete(String objectKey);
}
