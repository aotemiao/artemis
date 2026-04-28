package com.aotemiao.artemis.system.adapter.web.dto;

import java.time.LocalDateTime;

public record OperLogDTO(
        Long id,
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
        Long costTime,
        LocalDateTime operTime) {}
