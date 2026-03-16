package com.aotemiao.artemis.system.infra;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * 供 infra 模块内 {@code @DataJdbcTest} 等测试使用，满足 Spring 向上查找 @SpringBootConfiguration 的要求。
 * 主应用在 artemis-system-start，infra 测试 classpath 不包含 start，故在此提供占位配置。
 * 启用自动配置以便 JdbcRepositoriesAutoConfiguration 提供 JdbcMappingContext 等 Bean。
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class InfraTestConfiguration {
}
