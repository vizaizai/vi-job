package com.github.vizaizai.server.web.interceptor;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface HasRole {
    String value() default "";
}
