package com.aotemiao.artemis.framework.core.domain;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import java.io.Serializable;

/**
 * 统一 API 响应包装。
 *
 * @param <T> 载荷类型
 */
public record R<T>(String code, String message, T data) implements Serializable {

    public static <T> R<T> ok(T data) {
        return new R<>(CommonErrorCode.OK.getCode(), CommonErrorCode.OK.getMessage(), data);
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(String code, String message) {
        return new R<>(code, message, null);
    }

    public boolean isSuccess() {
        return CommonErrorCode.OK.getCode().equals(code);
    }
}
