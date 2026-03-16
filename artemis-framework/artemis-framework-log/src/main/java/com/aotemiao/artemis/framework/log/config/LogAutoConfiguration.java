package com.aotemiao.artemis.framework.log.config;

import com.aotemiao.artemis.framework.log.aspect.LogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(LogAspect.class)
public class LogAutoConfiguration {
}
