package com.github.vizaizai.server.constant;

/**
 * 公共常量
 * @author liaochongwei
 * @date 2023/5/18 16:04
 */
public class Commons {
    public static final String serverId = "vi-server";

    public static final String DT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * Timer最大等待时间-5min
     */
    public static final long TIMER_MAX = 1000L * 60 * 5;
    /**
     * Timer执行等待时间-30s
     */
    public static final long TIMER_MIN = 1000L * 30;
}
