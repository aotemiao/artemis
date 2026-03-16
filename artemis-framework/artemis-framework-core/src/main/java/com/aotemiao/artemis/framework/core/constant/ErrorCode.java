package com.aotemiao.artemis.framework.core.constant;

/**
 * Error code contract，用于统一异常处理。
 * 通过实现本接口或使用 enum 定义业务/系统错误码及可选的 HTTP 状态。
 */
public interface ErrorCode {

    String getCode();

    String getMessage();

    default int getHttpStatus() {
        return 500;
    }
}
