package com.darcklh.louise.Model.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 方法响应的命令
 * 此注解注解的方法将会响应命令插件前缀加上命令参数
 * e.g @OnCommand(commands = {"com1", "com2"}) 注解的方法将会响应 !{插件前缀} com1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnCommand {
    String[] commands() default {};
}
