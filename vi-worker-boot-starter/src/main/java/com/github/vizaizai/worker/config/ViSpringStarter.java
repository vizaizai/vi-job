package com.github.vizaizai.worker.config;

import com.github.vizaizai.worker.starter.ViStarter;

import javax.annotation.PreDestroy;

/**
 * @author liaochongwei
 * @date 2023/5/31 9:39
 */
public class ViSpringStarter extends ViStarter {
    @PreDestroy
    public void shutdown() {
        this.stop();
    }
}
