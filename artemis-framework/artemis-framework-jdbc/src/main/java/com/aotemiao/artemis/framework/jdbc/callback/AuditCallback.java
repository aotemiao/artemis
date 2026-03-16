package com.aotemiao.artemis.framework.jdbc.callback;

import java.util.Optional;

/**
 * 为 audit 字段提供当前操作人（用户 id/名称）。应用需实现该 Bean。
 */
@FunctionalInterface
public interface AuditCallback {

    /**
     * 当前操作人标识（如用户 id 或用户名），用于 createBy / updateBy。
     */
    Optional<String> currentAuditor();
}
