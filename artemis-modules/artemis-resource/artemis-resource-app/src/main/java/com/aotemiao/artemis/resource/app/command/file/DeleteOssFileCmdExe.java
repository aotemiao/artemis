package com.aotemiao.artemis.resource.app.command.file;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.file.ObjectStorageGateway;
import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 删除 OSS 文件命令执行器。 */
@Component
public class DeleteOssFileCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    private final OssFileGateway ossFileGateway;

    private final ObjectStorageGateway objectStorageGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    public DeleteOssFileCmdExe(OssFileGateway ossFileGateway, ObjectStorageGateway objectStorageGateway) {
        this.ossFileGateway = ossFileGateway;
        this.objectStorageGateway = objectStorageGateway;
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(DeleteOssFileCmd cmd) {
        OssFile ossFile = ossFileGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "OssFile not found: " + cmd.id()));
        objectStorageGateway.delete(ossFile.getObjectKey());
        ossFileGateway.deleteById(cmd.id());
    }
}
