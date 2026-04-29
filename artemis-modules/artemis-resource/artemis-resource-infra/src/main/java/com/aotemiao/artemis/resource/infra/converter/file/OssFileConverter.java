package com.aotemiao.artemis.resource.infra.converter.file;

import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import com.aotemiao.artemis.resource.infra.dataobject.file.OssFileDO;

/** OSS 文件 Domain / DO 转换器。 */
public final class OssFileConverter {

    private OssFileConverter() {}

    public static OssFileDO toDO(OssFile source) {
        if (source == null) {
            return null;
        }
        OssFileDO target = new OssFileDO();
        target.setId(source.getId());
        target.setFileName(source.getFileName());
        target.setOriginalFileName(source.getOriginalFileName());
        target.setSuffix(source.getSuffix());
        target.setUrl(source.getUrl());
        target.setUploader(source.getUploader());
        target.setProvider(source.getProvider());
        target.setObjectKey(source.getObjectKey());
        target.setSizeBytes(source.getSizeBytes());
        target.setExtJson(source.getExtJson());
        return target;
    }

    public static OssFile toDomain(OssFileDO source) {
        if (source == null) {
            return null;
        }
        OssFile target = new OssFile();
        target.setId(source.getId());
        target.setFileName(source.getFileName());
        target.setOriginalFileName(source.getOriginalFileName());
        target.setSuffix(source.getSuffix());
        target.setUrl(source.getUrl());
        target.setUploader(source.getUploader());
        target.setProvider(source.getProvider());
        target.setObjectKey(source.getObjectKey());
        target.setSizeBytes(source.getSizeBytes());
        target.setExtJson(source.getExtJson());
        return target;
    }
}
