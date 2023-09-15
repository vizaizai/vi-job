package com.github.vizaizai.common.contants;

/**
 * 业务编码
 * @author liaochongwei
 * @date 2023/4/24 14:39
 */
public class BizCode {

    /*==================================执行器==================================*/
    /**
     * 心跳检测
     */
    public static final String BEAT = "heart_beat";
    /**
     * 执行任务
     */
    public static final String EXEC = "exec";
    /**
     * 取消待执行任务
     */
    public static final String CANCEL = "cancel";
    /**
     * 空闲检测
     */
    public static final String IDlE = "idle";
    /**
     * 执行日志查询
     */
    public static final String LOG = "exec_log";
    /**
     * 查询执行状态
     */
    public static final String STATUS = "status";

    /*==================================调度中心==================================*/
    /**
     * 状态上报
     */
    public static final String REPORT = "report";
    /**
     * 运行任务
     */
    public static final String RUN = "run";

}
