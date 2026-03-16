package com.aotemiao.artemis.framework.jdbc.callback;

import com.aotemiao.artemis.framework.jdbc.base.Auditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 在 insert/update 前为 Auditable 实体填充 audit 字段（createTime、updateTime、createBy、updateBy）。
 */
public class AuditEntityCallback implements BeforeConvertCallback<Object>, Ordered {

    private final Optional<AuditCallback> auditCallback;

    public AuditEntityCallback(@Autowired(required = false) AuditCallback auditCallback) {
        this.auditCallback = Optional.ofNullable(auditCallback);
    }

    @Override
    public Object onBeforeConvert(Object entity) {
        if (!(entity instanceof Auditable auditable)) return entity;
        LocalDateTime now = LocalDateTime.now();
        String auditor = auditCallback.flatMap(AuditCallback::currentAuditor).orElse(null);
        if (auditable.getCreateTime() == null) {
            auditable.setCreateTime(now);
            auditable.setCreateBy(auditor);
        }
        auditable.setUpdateTime(now);
        auditable.setUpdateBy(auditor);
        return entity;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
