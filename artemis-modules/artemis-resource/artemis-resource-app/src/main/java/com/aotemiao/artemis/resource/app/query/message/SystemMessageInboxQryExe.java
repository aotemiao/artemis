package com.aotemiao.artemis.resource.app.query.message;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.message.SystemMessageGateway;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;

@Service
public class SystemMessageInboxQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final SystemMessageGateway systemMessageGateway;

    public SystemMessageInboxQryExe(SystemMessageGateway systemMessageGateway) {
        this.systemMessageGateway = systemMessageGateway;
    }

    public PageResult<SystemMessage> execute(SystemMessageInboxQry qry) {
        if (qry.recipientUserId() == null || qry.recipientUserId() <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Recipient user id must be positive");
        }
        return systemMessageGateway.findInbox(qry.recipientUserId(), qry.pageRequest());
    }
}
