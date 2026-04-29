package com.aotemiao.artemis.resource.app.query.file;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.file.ObjectStorageGateway;
import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.DownloadedObject;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 下载 OSS 文件查询执行器。 */
@Component
public class DownloadOssFileQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    private final OssFileGateway ossFileGateway;

    private final ObjectStorageGateway objectStorageGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    public DownloadOssFileQryExe(OssFileGateway ossFileGateway, ObjectStorageGateway objectStorageGateway) {
        this.ossFileGateway = ossFileGateway;
        this.objectStorageGateway = objectStorageGateway;
    }

    public DownloadedOssFile execute(DownloadOssFileQry qry) {
        OssFile ossFile = ossFileGateway
                .findById(qry.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "OssFile not found: " + qry.id()));
        DownloadedObject downloadedObject = objectStorageGateway.load(ossFile.getObjectKey());
        return new DownloadedOssFile(downloadedObject.fileName(), downloadedObject.content());
    }
}
