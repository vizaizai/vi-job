package com.github.vizaizai.worker.starter;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.remote.server.Server;
import com.github.vizaizai.remote.server.netty.NettyServer;
import com.github.vizaizai.remote.utils.NetUtils;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.worker.core.executor.IdleExecutor;
import com.github.vizaizai.worker.core.executor.TaskExecutor;
import com.github.vizaizai.worker.runner.JobProcessRunner;
import com.github.vizaizai.worker.runner.RegistryRunner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

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
     * 调度中心地址列表
     */
    private final List<String> severAddrList = new ArrayList<>();
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
            // 关闭任务运行
            JobProcessRunner.shutdownAll();
            // 关闭注册
            RegistryRunner.shutdown();
        }catch (Exception e) {
            logger.error("ShutDownFailure.",e);
        }
    }

    private void initEmbedServer() {
        // 获取主机地址
        this.host = this.host == null || this.host.trim().length() == 0 ? NetUtils.getLocalHost() : this.host;
        logger.info("address: {}", host+":" + port);
        Server server = new NettyServer(this.host, this.port);
        server.addBizProcessor(BizCode.RUN, new TaskExecutor());
        server.addBizProcessor(BizCode.IDlE, new IdleExecutor());
        server.start();
    }

    private void registry() {
        RegistryRunner.initAndStart(this.host + ":" + this.port,  this.appName, this.getSeverAddrList());
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
}
