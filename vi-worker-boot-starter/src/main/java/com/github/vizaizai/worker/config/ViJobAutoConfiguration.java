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

    @Bean
    @ConditionalOnMissingBean
    public ViStarter viStarter(ServerProperties serverProperties, WorkerProperties workerProperties) {
        ViSpringStarter starter = new ViSpringStarter();
        starter.setServerAddr(serverProperties.getAddress());
        starter.setAppName(workerProperties.getAppName());
        starter.setHost(workerProperties.getHost());
        starter.setPort(workerProperties.getPort());
        starter.setLogBasePath(workerProperties.getLogBasePath());
        starter.setLogMaxHistory(workerProperties.getLogMaxHistory());
        starter.start();
        return starter;
    }

    @Bean
    public JobMethodResolver jobMethodResolver() {
        return new SpringMethodResolver();
    }



}
