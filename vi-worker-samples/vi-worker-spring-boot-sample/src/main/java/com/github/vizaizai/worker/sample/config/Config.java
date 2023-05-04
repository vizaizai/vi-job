package com.github.vizaizai.worker.sample.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;

/**
 * @author liaochongwei
 * @date 2023/4/28 16:37
 */
@Configuration
public class Config {
    @Bean
    public FilterRegistrationBean<Filter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration publicConfig = new CorsConfiguration();
        publicConfig.setAllowCredentials(true);
        publicConfig.addAllowedOrigin("*");
        publicConfig.addAllowedHeader("*");
        publicConfig.addAllowedMethod("*");
        publicConfig.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", publicConfig); // CORS 配置对所有公共接口有效

        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }
}
