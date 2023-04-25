package com.github.vizaizai.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liaochongwei
 * @date 2023/4/25 10:42
 */
@ConfigurationProperties(
        prefix = "vi-job.server"
)
public class ServerProperties {

    /**
     * 调度服务地址，多个地址使用‘,’分隔
     */
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
