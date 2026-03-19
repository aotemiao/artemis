package com.aotemiao.artemis.framework.log.aspect;

import com.aotemiao.artemis.framework.log.annotation.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @Log 的 AOP 切面：记录操作（当前为 stub 实现）。
 */
@Aspect
@Component
@ConditionalOnProperty(name = "artemis.log.enabled", havingValue = "true", matchIfMissing = true)
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object result) {
        log.debug(
                "Operation log: {} - {}",
                controllerLog.title(),
                joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        log.warn("Operation log (exception): {} - {}", controllerLog.title(), e.getMessage());
    }
}
