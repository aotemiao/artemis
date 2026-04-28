package com.aotemiao.artemis.system.app.command.audit;

/** 记录后台操作日志命令。 */
public record RecordOperLogCmd(
        String title,
        String businessType,
        String method,
        String requestMethod,
        String operatorType,
        String operName,
        String deptName,
        String operUrl,
        String operIp,
        String operLocation,
        String operParam,
        String jsonResult,
        String status,
        String errorMsg,
        Long costTime) {}
