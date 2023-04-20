package com.darcklh.louise.Config;

import com.darcklh.louise.Filter.Handler.LouiseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * @author DarckLH
 * @date 2021/8/12 12:43
 * @Description
 */
@Configuration
public class HandlerConfig implements WebMvcConfigurer {

    @Autowired
    LouiseHandler louiseHandler;;

    static final String ORIGINS[] = new String[] { "GET", "POST", "PUT", "DELETE" };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(louiseHandler).addPathPatterns("/**").excludePathPatterns("/saito/**", "/error", "/templates/asserts/**", "/favicon.ico", "/saito_ws/**");
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOriginPatterns("*").allowCredentials(true).allowedMethods(ORIGINS).maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/templates/asserts/**").addResourceLocations("classpath:/asserts/");
    }
}
