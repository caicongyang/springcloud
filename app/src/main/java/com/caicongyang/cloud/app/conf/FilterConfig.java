package com.caicongyang.cloud.app.conf;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean ThreadLocalFilter(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new ThreadLocalFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
