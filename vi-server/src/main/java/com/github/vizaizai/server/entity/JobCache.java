package com.github.vizaizai.server.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务缓存实体
 * @author liaochongwei
 * @date 2023/5/19 14:39
 */
@Data
public class JobCache {
    /**
     * id
     */
    private Long id;
    /**
     * code
     */
    private String code;

    /**
     * 任务名称
     */
    private String name;
    /**
     * 执行器id
     */
    private Integer workerId;

    /**
     * 生命周期开始
     */
    private LocalDateTime startTime;

    /**
     * 生命周期结束
     */
    private LocalDateTime endTime;

    /**
     * 处理器类型 1-Bean 2-HTTP
     */
    private Integer processorType;
    /**
     * 处理器
     */
    private String processor;

    /**
     * 任务参数
     */
    private String param;

    /**
     * 触发类型 0-非主动触发 1-cron 2-固定频率（秒）3-固定延时（秒）
     */
    private Integer triggerType;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 频率
     */
    private Integer speedS;
    /**
     * 延时
     */
    private Integer delayedS;
    /**
     * 路由策略
     */
    private Integer routeType;

    /**
     * 任务失败重试次数
     */
    private Integer retryCount;
    /**
     * 任务超时时间
     */
    private Integer timeoutS;
    /**
     * 最大等待数量
     */
    private Integer maxWaitNum;
    /**
     * 任务实例自动删除时间（小时）
     */
    private Integer logAutoDelHours;
    /**
     * 下次触发时间
     */
    private Long nextTriggerTime;
}
