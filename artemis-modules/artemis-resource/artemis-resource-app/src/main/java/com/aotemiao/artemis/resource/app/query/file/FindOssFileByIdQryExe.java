package com.aotemiao.artemis.resource.app.query.file;

import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询 OSS 文件执行器。 */
@Component
public class FindOssFileByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final OssFileGateway ossFileGateway;

    public FindOssFileByIdQryExe(OssFileGateway ossFileGateway) {
        this.ossFileGateway = ossFileGateway;
    }

    public Optional<OssFile> execute(FindOssFileByIdQry qry) {
        return ossFileGateway.findById(qry.id());
    }
}
