package com.github.vizaizai.server.config;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.github.vizaizai.server.raft.RaftNodeOptions;
import com.github.vizaizai.server.raft.RaftServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;
import java.util.Objects;

/**
 * @author liaochongwei
 * @date 2023/4/28 16:37
 */
@EnableConfigurationProperties(ServerProperties.class)
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

    //@Bean
    public RaftServer raftServer(ServerProperties serverProperties, @Value("${server.port}") Integer port) {
        RaftServer raftServer = new RaftServer();
        if (Objects.equals(serverProperties.getMode(),"cluster")) {
            RaftNodeOptions options = new RaftNodeOptions();
            options.setGroupId("vi-server");
            options.setDataPath(serverProperties.getDataDir());
            options.setServerAddress(serverProperties.getInetutils().getIpAddress() + ":" + (port + 1000));
            options.setInitialServerAddressList(CollUtil.join(serverProperties.getCluster().getNodes(),","));
            raftServer.init(options);
        }
        return raftServer;
    }
}
