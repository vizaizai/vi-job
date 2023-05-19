package com.github.vizaizai.worker.starter;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.common.BizCode;
import com.github.vizaizai.remote.server.Server;
import com.github.vizaizai.remote.server.netty.NettyServer;
import com.github.vizaizai.remote.utils.NetUtils;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.worker.core.executor.TaskExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    /**
     * 任务线程池
     */
    private static final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(2, new BasicThreadFactory.Builder().namingPattern("Worker-Task-%d").build());

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
        // 检查参数
        this.checkParams();
        // 初始化内嵌服务
        this.initEmbedServer();
        // 注册调度中心
        this.registry();

    }

    public void stop() {
        try {
            // 关闭任务线程池
            scheduledExecutorService.shutdown();
            // 等待关闭
            if (!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        }catch (Exception e) {
            logger.error("ShutDownFailure.",e);
        }
    }

    private void initEmbedServer() {
        // 获取主机地址
        this.host = this.host == null || this.host.trim().length() == 0 ? NetUtils.getLocalHost() : this.host;
        logger.info("host: {}",host);
        Server server = new NettyServer(this.host,this.port);
        server.addBizProcessor(BizCode.RUN, new TaskExecutor());
        server.start(scheduledExecutorService);
    }

    private void registry() {

    }

    void checkParams() {
        if (Utils.isBlank(this.appName)) {
            throw new IllegalArgumentException("AppName invalid");
        }
        if (this.port == null) {
            throw new IllegalArgumentException("Port must be not null");
        }
        if (StringUtils.isBlank(serverAddr)) {
            throw new IllegalArgumentException("ServerAddr invalid");
        }
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
