package com.github.vizaizai.worker.starter;

import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.server.Server;
import com.github.vizaizai.remote.server.netty.NettyServer;
import com.github.vizaizai.remote.utils.NetUtils;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.worker.core.executor.*;
import com.github.vizaizai.worker.runner.JobProcessRunner;
import com.github.vizaizai.worker.runner.LogClearScheduledRunner;
import com.github.vizaizai.worker.runner.RegistryScheduledRunner;
import com.github.vizaizai.worker.runner.ReportRetryRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
     * 地址，默认由系统获取
     */
    private String host;
    /**
     * 服务端口号
     */
    private Integer port;
    /**
     * 日志路径
     */
    private String logBasePath;
    /**
     * 日志最大保留天数
     */
    private Integer logMaxHistory;
    /**
     * 调度中心地址列表
     */
    private final List<String> severAddrList = new ArrayList<>();
    /**
     * scheduled线程池
     */
    private final static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2,
            new BasicThreadFactory.Builder().namingPattern("Scheduled-%d").build());


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
        this.initRegistry();
        // 日志清理
        this.initLogClear();
        // 上报重试
        this.initReportRetry();

    }

    public void stop() {
        try {
            // 关闭任务运行
            JobProcessRunner.shutdownAll();
            // 关闭注册
            RegistryScheduledRunner.shutdown();
            // 关闭日志清理
            LogClearScheduledRunner.shutdown();

        }catch (Exception e) {
            logger.error("ShutDownFailure.",e);
        }
    }

    private void initEmbedServer() {
        // 获取主机地址
        this.host = this.host == null || this.host.trim().length() == 0 ? NetUtils.getLocalHost() : this.host;
        Server server = new NettyServer(this.host, this.port);
        server.addBizProcessor(BizCode.RUN, new TaskExecutor());
        server.addBizProcessor(BizCode.IDlE, new IdleExecutor());
        server.addBizProcessor(BizCode.LOG, new LogExecutor());
        server.addBizProcessor(BizCode.CANCEL, new ExecCancelExecutor());
        server.addBizProcessor(BizCode.STATUS, new ExecStatusExecutor());
        server.start();
    }

    private void initRegistry() {
        RegistryScheduledRunner.initAndStart(this.host + ":" + this.port,  this.appName,
                this.getSeverAddrList(), scheduledExecutorService);
    }

    private void initLogClear() {
        LogClearScheduledRunner.initAndStart(this.logBasePath, this.logMaxHistory, scheduledExecutorService);
    }

    private void initReportRetry() {
        ReportRetryRunner.getInstance();
    }

    void checkParams() {
        if (Utils.isBlank(this.appName)) {
            throw new IllegalArgumentException("AppName invalid");
        }
        if (this.port == null) {
            throw new IllegalArgumentException("Port must be not null");
        }
        if (StringUtils.isBlank(serverAddr.trim())) {
            throw new IllegalArgumentException("ServerAddr invalid");
        }
        if (StringUtils.isBlank(logBasePath)) {
            throw new IllegalArgumentException("LogBasePath invalid");
        }
        if (this.logMaxHistory == null) {
            throw new IllegalArgumentException("logMaxHistory must be not null");
        }

        String[] addrArray = serverAddr.split(",");
        for (String addr : addrArray) {
            addr = addr.trim();
            if (addr.startsWith("http://") || addr.startsWith("https://")) {
                severAddrList.add(addr);
            }else {
                if (StringUtils.isNotBlank(addr)) {
                    addr = "http://" + addr;
                    severAddrList.add(addr);
                }
            }
        }
        if (Utils.isEmpty(this.severAddrList)) {
            throw new IllegalArgumentException("ServerAddr invalid");
        }
        System.setProperty(PropsKeys.LOG_BASE_PATH, logBasePath);
        System.setProperty(PropsKeys.SERVER_ADDR, String.join(",", severAddrList));
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

    public List<String> getSeverAddrList() {
        return severAddrList;
    }

    public String getLogBasePath() {
        return logBasePath;
    }

    public void setLogBasePath(String logBasePath) {
        this.logBasePath = logBasePath;
    }

    public Integer getLogMaxHistory() {
        return logMaxHistory;
    }

    public void setLogMaxHistory(Integer logMaxHistory) {
        this.logMaxHistory = logMaxHistory;
    }
}
