package com.github.vizaizai.worker.runner;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.common.model.StatusCode;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.worker.utils.HttpUtils;
import com.github.vizaizai.worker.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 注册表运行器
 * @author liaochongwei
 * @date 2023/5/9 16:26
 */
public class RegistryScheduledRunner implements Runnable {
   private static final Logger logger = LoggerFactory.getLogger(RegistryScheduledRunner.class);
    /**
     * 注册地址
     */
    private String address;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 调度中心地址列表
     */
    private List<String> serverAddrList;

    private ScheduledFuture<?> scheduledFuture;
    /**
     * 注册地址
     */
    private static final String REGISTER_URL = "/worker/register";
    /**
     * 注销地址
     */
    private static final String UNREGISTER_URL = "/worker/unregister";
    private static RegistryScheduledRunner runner;
    private RegistryScheduledRunner() {
    }


    /**
     * 初始化并启动
     * @param address 注册地址
     * @param appName 应用名称
     * @param serverAddrList 调度中心地址列表
     */
    public static void initAndStart(String address, String appName,
                                    List<String> serverAddrList, ScheduledExecutorService scheduledExecutorService) {
        if (runner != null) {
            return;
        }
        runner = new RegistryScheduledRunner();
        runner.address = address;
        runner.appName = appName;
        runner.serverAddrList = serverAddrList;
        runner.scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(runner, 1, 60, TimeUnit.SECONDS);
        logger.debug(">>>>>>>>>>RegistryRunner started, serverAddr:{}", String.join(",", serverAddrList));

    }
    @Override
    public void run() {
        for (String serverAddr : serverAddrList) {
            boolean success = this.process(serverAddr + REGISTER_URL);
            if (success) {
                logger.debug(">>>>>>>>>>Worker register success.");
                break;
            }
            logger.warn("Worker register fail, serverAddr:{}", serverAddr);
        }
    }

    public static void shutdown() {
        try {
            RegistryScheduledRunner runner = RegistryScheduledRunner.runner;
            if (runner != null) {
                for (String serverAddr : runner.serverAddrList) {
                    boolean success = runner.process(serverAddr + UNREGISTER_URL);
                    if (success) {
                        logger.debug("Worker unregister success.");
                        break;
                    }
                }
                runner.scheduledFuture.cancel(true);
            }

        }catch (Exception e) {
            logger.error("Shutdown RegistryRunner error,", e);
        }
    }

    /**
     * worker注册或注销
     */
    @SuppressWarnings("all")
    private boolean process(String url) {
        Map<String,String> body = new HashMap<>();
        body.put("appName", appName);
        body.put("address", address);
        try {
            String resultStr = HttpUtils.doPost(url, body, 15000);
            if (StringUtils.isBlank(resultStr)) {
                return false;
            }
            Result result = JSONUtils.parseObject(resultStr, Result.class);
            if (Objects.equals(result.getCode(), StatusCode.SUCCESS.getCode())) {
                return true;
            }
            return false;

        }catch (Exception e) {
            logger.error("Worker process error,", e);
        }
        return false;
    }

}
