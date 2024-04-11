package com.darcklh.louise.Utils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringContextUtils implements ApplicationContextAware {
    private static ApplicationContext context;
    private static final Logger log = LoggerFactory.getLogger(SpringContextUtils.class);
    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        SpringContextUtils.context = context;
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static Object getBean(String beanName) {
        try {
           return context.getBean(beanName);
        } catch (BeansException e) {
            log.error("获取 {} 对象异常: {}", beanName, e.getLocalizedMessage());
            return null;
        }
    }
}
