package com.darcklh.louise.Config;

import com.darcklh.louise.Filter.LouiseFilter;
import com.darcklh.louise.Filter.SaitoFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author DarckLH
 * @date 2021/8/12 12:20
 * @Description
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean registerLouiseFilter() {

        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new LouiseFilter());
        registrationBean.addUrlPatterns("/louise/*");
        registrationBean.setOrder(2);
        return registrationBean;

    }

    @Bean
    public FilterRegistrationBean registerSaitoFilter() {

        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new SaitoFilter());
        registrationBean.addUrlPatterns("/saito/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
