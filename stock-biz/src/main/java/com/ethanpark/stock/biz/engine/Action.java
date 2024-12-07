package com.ethanpark.stock.biz.engine;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Author: 柏云鹏 Date: 2022/4/20.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Action {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
