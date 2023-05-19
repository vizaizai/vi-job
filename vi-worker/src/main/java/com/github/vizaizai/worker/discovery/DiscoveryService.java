package com.github.vizaizai.worker.discovery;

import com.github.vizaizai.remote.utils.NetUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 服务发现
 * @author liaochongwei
 * @date 2023/5/10 11:35
 */
public class DiscoveryService {
    /**
     * 地址列表
     */
    private final List<String> serverAddrList;
    /**
     * 应用名称
     */
    private final String appName;
    /**
     * 当前调度地址
     */
    private String currentServerAddress;
    /**
     * serverAddr->host
     */
    private Map<String, String> hostMap = new HashMap<>();

    public DiscoveryService(List<String> serverAddrList, String appName) {
        this.serverAddrList = serverAddrList;
        this.appName = appName;
    }

    public void start(ScheduledExecutorService scheduledExecutorService) {
        // 随机取一个
        String currentServerAddress = serverAddrList.get(ThreadLocalRandom.current().nextInt(serverAddrList.size()) - 1);



        scheduledExecutorService.scheduleWithFixedDelay(this::choose,10,60, TimeUnit.SECONDS);
    }
    /**
     * 选择一个地址
     */
    public void choose() {
        String host = NetUtils.getLocalHost();

    }


}
