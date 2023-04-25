package com.github.vizaizai.worker.core.annotation;

import java.lang.annotation.*;

/**
 * 任务方法注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Job {
    /**
     * 处理器名称
     */
    String value();
}
