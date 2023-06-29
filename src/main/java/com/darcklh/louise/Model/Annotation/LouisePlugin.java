package com.darcklh.louise.Model.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * LouisePlugin 注解
 * 此注解注解的类预置一些基础信息
 * e.g @LouisePlugin(prefix = "plugin") 此插件的所有方法会把 "plugin" 作为插件前缀
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LouisePlugin {
    String prefix() default "";
}
