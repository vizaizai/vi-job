package com.github.vizaizai.worker.starter;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.common.BizCode;
import com.github.vizaizai.remote.server.Server;
import com.github.vizaizai.remote.server.netty.NettyServer;
import com.github.vizaizai.remote.utils.NetUtils;
import com.github.vizaizai.worker.core.executor.TaskExecutor;
import org.slf4j.Logger;

/**
 * 启动器
 * @author liaochongwei
 * @date 2023/4/24 10:30
 */
public class ViStarter {
    private static final Logger logger = LoggerFactory.getLogger(ViStarter.class);
    /**
     * 应用名称
     */
    private String appName;
    /**
     * vi-server地址，集群用逗号隔开
     */
    private String serverAddr;
    /**
     * 本地地址，默认由系统获取
     */
    private String host;
    /**
     * 本地服务端口号
     */
    private Integer port;

    public String getAppName() {
        return appName;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public void start() {
        // 初始化内嵌服务
        this.initEmbedServer();
        // 注入到调度服务
        this.registryServer();

    }

    private void initEmbedServer() {
        // 获取主机地址
        this.host = this.host == null || this.host.trim().length() == 0 ? NetUtils.getLocalHost() : this.host;
        if (this.port == null) {
            throw new IllegalArgumentException("Port must be not null");
        }
        logger.info("host: {}",host);
        Server server = new NettyServer(this.host,this.port);
        server.addBizProcessor(BizCode.RUN, new TaskExecutor());
        server.start();
    }

    private void registryServer() {

    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
