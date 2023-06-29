package com.darcklh.louise.Model.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消息响应的命令
 * 此注解注解的方法将使用正则表达式匹配发送的消息
 * e.g @OnMessage(messages = {"^你好", "我不好$"}) 注解的方法将会响应 以“你好”开头或“我不好”结尾的消息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnMessage {
    String[] messages() default {};
}
