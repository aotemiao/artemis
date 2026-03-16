package com.aotemiao.artemis.framework.jdbc.config;

import com.aotemiao.artemis.framework.jdbc.callback.AuditCallback;
import com.aotemiao.artemis.framework.jdbc.callback.AuditEntityCallback;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;

import java.util.Optional;

/**
 * Spring Data JDBC 自动配置：审计回调 + 仓库扫描。
 * 仓库扫描包通过配置项 {@code artemis.jdbc.repositories.base-packages} 指定（与 RuoYi 的 mybatis-plus.mapperPackage 思路一致）。
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.data.jdbc.core.JdbcAggregateTemplate")
@ConditionalOnProperty(name = "artemis.jdbc.repositories.base-packages")
@EnableJdbcRepositories("${artemis.jdbc.repositories.base-packages}")
public class JdbcAutoConfiguration {

    @Bean
    public BeforeConvertCallback<Object> auditEntityCallback(Optional<AuditCallback> auditCallback) {
        return new AuditEntityCallback(auditCallback.orElse(null));
    }
}
