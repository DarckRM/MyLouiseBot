package com.darcklh.louise.Model.Annotation;

import com.darcklh.louise.Model.GoCqhttp.NoticePost;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通知响应的命令
 * 此注解注解的方法将响应枚举出的通知
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnNotice {
    NoticePost.NoticeType[] notices() default {};
}
