package com.aotemiao.artemis.framework.log.annotation;

import java.lang.annotation.*;

/** 标记方法需要记录操作审计日志。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String title() default "";

    String businessType() default "";
}
