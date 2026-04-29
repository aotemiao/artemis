package com.aotemiao.artemis.resource.adapter.dubbo;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.app.command.file.UploadOssFileCmd;
import com.aotemiao.artemis.resource.app.command.file.UploadOssFileCmdExe;
import com.aotemiao.artemis.resource.app.query.file.FindOssFileByIdQry;
import com.aotemiao.artemis.resource.app.query.file.FindOssFileByIdQryExe;
import com.aotemiao.artemis.resource.app.query.file.ListOssFilesByIdsQry;
import com.aotemiao.artemis.resource.app.query.file.ListOssFilesByIdsQryExe;
import com.aotemiao.artemis.resource.client.api.ResourceFileService;
import com.aotemiao.artemis.resource.client.dto.FileUrlResponse;
import com.aotemiao.artemis.resource.client.dto.UploadFileRequest;
import com.aotemiao.artemis.resource.client.dto.UploadedFileResponse;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.apache.dubbo.config.annotation.DubboService;

/** 资源文件 Dubbo 服务实现。 */
@DubboService
public class ResourceFileServiceDubboImpl implements ResourceFileService {

    private final UploadOssFileCmdExe uploadOssFileCmdExe;
    private final FindOssFileByIdQryExe findOssFileByIdQryExe;
    private final ListOssFilesByIdsQryExe listOssFilesByIdsQryExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects executors as managed collaborators; this adapter does not expose them.")
    public ResourceFileServiceDubboImpl(
            UploadOssFileCmdExe uploadOssFileCmdExe,
            FindOssFileByIdQryExe findOssFileByIdQryExe,
            ListOssFilesByIdsQryExe listOssFilesByIdsQryExe) {
        this.uploadOssFileCmdExe = uploadOssFileCmdExe;
        this.findOssFileByIdQryExe = findOssFileByIdQryExe;
        this.listOssFilesByIdsQryExe = listOssFilesByIdsQryExe;
    }

    @Override
    public UploadedFileResponse upload(UploadFileRequest request) {
        OssFile ossFile = uploadOssFileCmdExe.execute(new UploadOssFileCmd(
                request.originalFileName(), request.content(), request.uploader(), request.extJson()));
        return new UploadedFileResponse(
                ossFile.getId(),
                ossFile.getFileName(),
                ossFile.getOriginalFileName(),
                ossFile.getUrl(),
                ossFile.getProvider());
    }

    @Override
    public FileUrlResponse getUrl(Long fileId) {
        OssFile ossFile = findOssFileByIdQryExe
                .execute(new FindOssFileByIdQry(fileId))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "OssFile not found: " + fileId));
        return new FileUrlResponse(ossFile.getId(), ossFile.getUrl());
    }

    @Override
    public List<FileUrlResponse> listByIds(List<Long> fileIds) {
        return listOssFilesByIdsQryExe.execute(new ListOssFilesByIdsQry(fileIds)).stream()
                .map(ossFile -> new FileUrlResponse(ossFile.getId(), ossFile.getUrl()))
                .toList();
    }
}
