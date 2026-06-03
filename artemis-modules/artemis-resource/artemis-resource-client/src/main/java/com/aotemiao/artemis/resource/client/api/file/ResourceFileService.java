package com.aotemiao.artemis.resource.client.api.file;

import com.aotemiao.artemis.resource.client.dto.file.FileUrlResponse;
import com.aotemiao.artemis.resource.client.dto.file.UploadFileRequest;
import com.aotemiao.artemis.resource.client.dto.file.UploadedFileResponse;
import java.util.List;

/** 资源文件远程服务。 */
public interface ResourceFileService {

    UploadedFileResponse upload(UploadFileRequest request);

    FileUrlResponse getUrl(Long fileId);

    List<FileUrlResponse> listByIds(List<Long> fileIds);
}
