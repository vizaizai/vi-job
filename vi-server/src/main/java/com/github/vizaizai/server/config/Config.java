package com.github.vizaizai.server.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;

/**
 * @author liaochongwei
 * @date 2023/4/28 16:37
 */
@EnableScheduling
@Configuration
@MapperScan("com.github.vizaizai.server.dao")
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

    @Bean
    public CustomMetaObjectHandler metaObjectHandler() {
        return new CustomMetaObjectHandler();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
