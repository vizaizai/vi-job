package com.github.vizaizai.server.config;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.github.vizaizai.server.web.interceptor.PermInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liaochongwei
 * @date 2023/4/28 16:37
 */
@Slf4j
@EnableConfigurationProperties(ServerProperties.class)
@EnableScheduling
@Configuration
@MapperScan("com.github.vizaizai.server.dao")
public class Config implements WebMvcConfigurer {

    @Bean("executor")
    public ThreadPoolExecutor jobExecutor() {
        ThreadPoolExecutor  executor = new ThreadPoolExecutor(0,200,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(500));
        // 设置拒绝策略 对拒绝任务直接无声抛弃，没有异常信息。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
		// 注册AuthorizationInterceptor 类
        InterceptorRegistration registration = registry.addInterceptor(new PermInterceptor());
        // 所有的路径都要
        registration.addPathPatterns("/**");
    }

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
    public IdentifierGenerator idGenerator(ServerProperties serverProperties) {
        // 机器号
        long workerId = RandomUtil.randomInt(0,31);
        // 序列号
        long dataCenterId = RandomUtil.randomInt(0,31);
        try {
            // 集群部署
            if (Objects.equals("cluster",serverProperties.getMode())) {
                List<String> nodes = serverProperties.getCluster().getNodes();
                String ipAddress = serverProperties.getInetutils().getIpAddress();
                int indexOf = nodes.indexOf(ipAddress);
                if (indexOf != -1) {
                    int k = 0;
                    for (int i = 0; i < 32; i++) {
                        for (int j = 0; j < 32; j++) {
                            if (k == indexOf) {
                                workerId = i;
                                dataCenterId = j;
                            }
                        }
                    }
                }
            }
        }catch (Exception e) {
            log.error("ID生成参数设置错误:", e);
        }

        log.debug("ID生成参数分配成功，workerId：{}，dataCenterId：{}", workerId, dataCenterId);
        return new DefaultIdentifierGenerator(workerId, dataCenterId);
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
