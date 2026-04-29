package com.aotemiao.artemis.resource.app.command.file;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.file.ObjectStorageGateway;
import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import com.aotemiao.artemis.resource.domain.model.file.StoredObject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 上传 OSS 文件命令执行器。 */
@Component
public class UploadOssFileCmdExe {

    private static final String DEFAULT_UPLOADER = "system";
    private static final String LOCAL_PROVIDER = "LOCAL";

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    private final OssFileGateway ossFileGateway;

    private final ObjectStorageGateway objectStorageGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    public UploadOssFileCmdExe(OssFileGateway ossFileGateway, ObjectStorageGateway objectStorageGateway) {
        this.ossFileGateway = ossFileGateway;
        this.objectStorageGateway = objectStorageGateway;
    }

    @Transactional(rollbackFor = Exception.class)
    public OssFile execute(UploadOssFileCmd cmd) {
        if (cmd.content().length == 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Upload file must not be empty");
        }
        StoredObject storedObject = objectStorageGateway.store(cmd.originalFileName(), cmd.content());
        OssFile ossFile = new OssFile();
        ossFile.setFileName(storedObject.fileName());
        ossFile.setOriginalFileName(normalizeOriginalFileName(cmd.originalFileName()));
        ossFile.setSuffix(storedObject.suffix());
        ossFile.setUrl(storedObject.url());
        ossFile.setUploader(normalizeUploader(cmd.uploader()));
        ossFile.setProvider(LOCAL_PROVIDER);
        ossFile.setObjectKey(storedObject.objectKey());
        ossFile.setSizeBytes(storedObject.sizeBytes());
        ossFile.setExtJson(normalizeText(cmd.extJson()));
        return ossFileGateway.save(ossFile);
    }

    private String normalizeOriginalFileName(String originalFileName) {
        String normalized = normalizeText(originalFileName);
        return normalized == null ? "unnamed" : normalized;
    }

    private String normalizeUploader(String uploader) {
        String normalized = normalizeText(uploader);
        return normalized == null ? DEFAULT_UPLOADER : normalized;
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}
