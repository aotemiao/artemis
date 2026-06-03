package com.aotemiao.artemis.system.adapter.web.audit;

import com.aotemiao.artemis.system.app.command.audit.RecordOperLogCmd;
import com.aotemiao.artemis.system.app.command.audit.RecordOperLogCmdExe;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 后台操作日志切面。 */
@Aspect
@Component
public class OperLogAspect {

    private static final int MAX_TEXT_LENGTH = 2_000;

    private final RecordOperLogCmdExe recordOperLogCmdExe;
    private final ObjectMapper objectMapper;

    public OperLogAspect(RecordOperLogCmdExe recordOperLogCmdExe, ObjectMapper objectMapper) {
        this.recordOperLogCmdExe = recordOperLogCmdExe;
        this.objectMapper = objectMapper.copy();
    }

    @Around("@annotation(com.aotemiao.artemis.system.adapter.web.audit.OperLogRecord)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable failure = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            record(joinPoint, result, failure, System.currentTimeMillis() - start);
        }
    }

    private void record(ProceedingJoinPoint joinPoint, Object result, Throwable failure, long costTime) {
        try {
            OperLogRecord annotation =
                    ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(OperLogRecord.class);
            HttpServletRequest request = currentRequest();
            recordOperLogCmdExe.execute(new RecordOperLogCmd(
                    annotation.title(),
                    annotation.businessType(),
                    joinPoint.getSignature().toShortString(),
                    request == null ? null : request.getMethod(),
                    annotation.operatorType(),
                    currentOperator(request),
                    request == null ? null : request.getHeader("X-Dept-Name"),
                    request == null ? null : request.getRequestURI(),
                    clientIp(request),
                    "未知",
                    serialize(joinPoint.getArgs()),
                    failure == null ? serialize(result) : null,
                    failure == null ? "SUCCESS" : "FAIL",
                    failure == null ? null : truncate(failure.getMessage()),
                    costTime));
        } catch (RuntimeException ignored) {
            // 操作日志记录失败不能阻断主业务请求。
        }
    }

    private HttpServletRequest currentRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String currentOperator(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String username = request.getHeader("X-Username");
        return username == null || username.isBlank() ? "unknown" : username;
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String serialize(Object value) {
        try {
            return truncate(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException ex) {
            return truncate(String.valueOf(value));
        }
    }

    private String serialize(Object[] args) {
        return serialize(Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest))
                .toList());
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_TEXT_LENGTH);
    }
}
