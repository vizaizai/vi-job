package com.github.vizaizai.worker.config;

import com.github.vizaizai.worker.core.processor.method.JobMethodResolver;
import com.github.vizaizai.worker.processor.SpringMethodResolver;
import com.github.vizaizai.worker.starter.ViStarter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liaochongwei
 * @date 2023/4/25 10:41
 */
@Configuration
@EnableConfigurationProperties({ServerProperties.class, WorkerProperties.class})
public class ViJobAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public ViStarter viStarter(ServerProperties serverProperties, WorkerProperties workerProperties) {
        ViStarter viStarter = new ViStarter();
        viStarter.setServerAddr(serverProperties.getAddress());
        viStarter.setAppName(workerProperties.getAppName());
        viStarter.setHost(workerProperties.getHost());
        viStarter.setPort(workerProperties.getPort());
        viStarter.start();
        return viStarter;
    }

    @Bean
    public JobMethodResolver jobMethodResolver() {
        return new SpringMethodResolver();
    }



}
