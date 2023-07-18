package com.github.vizaizai.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liaochongwei
 * @date 2023/4/25 10:42
 */
@ConfigurationProperties(
        prefix = "vi-job.worker"
)
public class WorkerProperties {

    /**
     * 应用名称
     */
    private String appName;
    /**
     * 主机地址，默认由系统获取
     */
    private String host;
    /**
     * 端口号，默认3923
     */
    private Integer port = 3923;

    private String logBasePath = "/data/vi-job";

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLogBasePath() {
        return logBasePath;
    }

    public void setLogBasePath(String logBasePath) {
        this.logBasePath = logBasePath;
    }
}
