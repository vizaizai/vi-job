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
     * Timer最大等待时间5min
     */
    public static final long TIMER_MAX = 1000L * 60 * 5;
    /**
     * Timer执行等待时间30s
     */
    public static final long TIMER_MIN = 1000L * 30;
    /**
     * 快照名
     */
    public static final String SNAPSHOT_NAME = "data";


    /**
     * 注册表失效失效（秒）
     */
    public static final long REG_EXPIRED_S = 120;
    /**
     * 看门狗最大空闲（秒）
     */
    public static final long WATCH_MAX_IDLE = 60 * 1000;
    /**
     * job分配信息key
     */
    public static final String JOB_ASSIGN_KEY = "job:assign:info";
    /**
     * 执行器节点信息key
     */
    public static final String WORKER_NODE_KEY = "worker:node:info:";
    /**
     * job key
     */
    public static final String JOB_KEY = "job:info:";
}
