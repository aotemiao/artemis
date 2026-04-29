package com.aotemiao.artemis.resource.app.query.file;

import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 按 ID 列表查询 OSS 文件执行器。 */
@Component
public class ListOssFilesByIdsQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final OssFileGateway ossFileGateway;

    public ListOssFilesByIdsQryExe(OssFileGateway ossFileGateway) {
        this.ossFileGateway = ossFileGateway;
    }

    public List<OssFile> execute(ListOssFilesByIdsQry qry) {
        return ossFileGateway.findByIds(qry.ids());
    }
}
