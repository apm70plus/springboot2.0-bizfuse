package com.apm70.bizfuse.tcc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TCC事务的注释，用在TCC的Try方法上，框架会基于AOP织入TCC事务
 * 
 * @author liuyg
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TccTransaction {
}
