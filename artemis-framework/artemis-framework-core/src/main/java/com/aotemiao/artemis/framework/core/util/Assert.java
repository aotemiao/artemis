package com.aotemiao.artemis.framework.core.util;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;

/**
 * 防御式编程用 fail-fast 断言。
 */
public final class Assert {

    private Assert() {
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST.getCode(), message, 400);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BizException(CommonErrorCode.BAD_REQUEST.getCode(), message, 400);
        }
    }

    public static void notBlank(String str, String message) {
        if (str == null || str.isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST.getCode(), message, 400);
        }
    }
}
