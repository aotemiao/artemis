package com.aotemiao.artemis.system.adapter;

import org.springframework.boot.SpringBootConfiguration;

/**
 * 供 adapter 模块内 {@code @WebMvcTest} 等测试使用，满足 Spring 向上查找 @SpringBootConfiguration 的要求。
 * 主应用在 artemis-system-start，adapter 测试 classpath 不包含 start，故在此提供占位配置。
 * 不启用 EnableAutoConfiguration，由 @WebMvcTest 的 slice 控制加载范围，确保 Controller 映射生效。
 * <p>当前 LookupTypeControllerTest 使用 standaloneSetup，未使用本配置；保留供后续 @WebMvcTest 或集成测试使用。</p>
 */
@SpringBootConfiguration
public class AdapterTestConfiguration {
}
