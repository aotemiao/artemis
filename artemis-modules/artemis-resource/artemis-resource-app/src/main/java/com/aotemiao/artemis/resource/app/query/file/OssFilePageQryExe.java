package com.aotemiao.artemis.resource.app.query.file;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** OSS 文件分页查询执行器。 */
@Component
public class OssFilePageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final OssFileGateway ossFileGateway;

    public OssFilePageQryExe(OssFileGateway ossFileGateway) {
        this.ossFileGateway = ossFileGateway;
    }

    public PageResult<OssFile> execute(OssFilePageQry qry) {
        return ossFileGateway.findPage(qry.pageRequest());
    }
}
