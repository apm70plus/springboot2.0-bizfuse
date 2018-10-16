package com.apm70.bizfuse.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 可以从JsonBody中获取变量, 但是非Spring自带, 使用时请考虑兼容性
 * 
 * @author liuyg
 */
@Target({java.lang.annotation.ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BodyVariable {

    @AliasFor("name")
    public abstract String value() default "";

    @AliasFor("value")
    public abstract String name() default "";

    public abstract boolean required() default false;

    public abstract String defaultValue() default "";
}
