package com.github.vizaizai.worker.starter;

import java.io.File;

/**
 * 公共常量
 * @author liaochongwei
 * @date 2023/8/8 17:08
 */
public class Commons {
    /**
     * 重试参数路径
     */
    public static final String RETRY_PATH = "retry";
    public static final String RETRY_SUFFIX = ".srp";
    /**
     * 最大空闲时间
     */
    public static final Integer MAX_IDLE = 120 * 1000;

    public static String getLogBasePath() {
        return System.getProperty(PropsKeys.LOG_BASE_PATH, "/data/vi-job");
    }

    public static String getRetryPath() {
        return getLogBasePath() + File.separator + RETRY_PATH;
    }

    public static String getServerAddr() {
        return System.getProperty(PropsKeys.SERVER_ADDR);
    }
}
