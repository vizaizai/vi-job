package com.github.vizaizai.server.entity;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.github.vizaizai.retry.mode.CronSequenceGenerator;
import com.github.vizaizai.server.constant.TriggerType;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

/**
 * 任务实体
 * @author liaochongwei
 * @date 2023/5/19 14:39
 */
@Data
public class Job {
    /**
     * id
     */
    private Long id;

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
     * 任务失败重试次数(-1:不限制)
     */
    private Integer retryCount;
    /**
     * 任务超时时间(-1:不限制)
     */
    private Integer timeoutS;
    /**
     * 任务超时处理策略 1-标记 2-中断
     */
    private Integer timeoutHandleType;
    /**
     * 上一次触发时间
     */
    private Long lastTriggerTime;
    /**
     * 上一次执行结束时间
     */
    private Long lastExecuteEndTime;
    /**
     * 执行器地址列表
     */
    private List<String> workerAddressList;


    // private static final Set<String> delayedJobSet = Collections.synchronizedSet(new HashSet<>());
    /**
     * 计算下一次触发时间
     * @return 时间戳
     */
    public Long getNextTriggerTime() {
        long now = System.currentTimeMillis();
        LocalDateTime nowDateTime = LocalDateTimeUtil.of(now);
        // 未开始
        if (startTime != null && nowDateTime.isBefore(startTime)) {
            return getNextTriggerTime0(LocalDateTimeUtil.toEpochMilli(startTime));
        }
        // 已结束
        if (endTime != null && nowDateTime.isAfter(endTime)) {
            return null;
        }
        return getNextTriggerTime0(now);
    }
    /**
     * 计算下一次触发时间
     * @return 时间戳
     */
    public Long getNextTriggerTime0(Long dateTime) {
        if (triggerType == TriggerType.NON.getCode()) {
            return null;
        }
        // cron
        if (triggerType == TriggerType.CRON.getCode()){
            final CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(cron);
            Date next = cronSequenceGenerator.next(new Date(dateTime));
            return next.getTime();
        }
        // 固定频率
        if (triggerType == TriggerType.SPEED.getCode()) {
            Long time = lastTriggerTime != null ? lastTriggerTime : dateTime;
            return time + speedS * 1000;
        }
        // 固定延时，需要基于上一次执行完成时间
        if (triggerType == TriggerType.DELAYED.getCode()) {
            Long time = lastTriggerTime != null ? lastTriggerTime : dateTime;
            if (lastExecuteEndTime == null) {
                return time + delayedS  * 1000;
            }
            // 上一次未执行完成
            return lastExecuteEndTime + delayedS * 1000;
        }

        return null;
    }

}
