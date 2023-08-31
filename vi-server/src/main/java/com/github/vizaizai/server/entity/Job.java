package com.github.vizaizai.server.entity;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.github.vizaizai.retry.mode.CronSequenceGenerator;
import com.github.vizaizai.server.constant.TriggerType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

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
    /**
     * 直接运行
     */
    private boolean directRun;
    /**
     * 任务实例id
     */
    private Long instanceId;
    /**
     * 基准时间
     */
    private Long baseTime;
    /**
     * 已取消执行
     */
    private boolean canceled;

    public Job resetNextTriggerTime() {
        this.nextTriggerTime = this.initNextTriggerTime();
        return this;
    }

    /**
     * 计算下一次触发时间
     * @return 时间戳
     */
    public Long initNextTriggerTime() {
        if (baseTime == null) {
            this.baseTime = System.currentTimeMillis();
        }
        LocalDateTime baseLocalTime = LocalDateTimeUtil.of(this.baseTime);
        // 未开始
        if (startTime != null && baseLocalTime.isBefore(startTime)) {
            this.baseTime = LocalDateTimeUtil.toEpochMilli(startTime);
            return initNextTriggerTime0(baseTime);
        }
        // 已结束
        if (endTime != null && baseLocalTime.isAfter(endTime)) {
            return null;
        }
        return initNextTriggerTime0(this.baseTime);
    }
    /**
     * 计算下一次触发时间
     * @return 时间戳
     */
    public Long initNextTriggerTime0(long dateTime) {
        if (triggerType == TriggerType.NON.getCode()) {
            return null;
        }
        // cron
        if (triggerType == TriggerType.CRON.getCode()){
            try {
                final CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(cron);
                Date next = cronSequenceGenerator.next(new Date(dateTime));
                return next.getTime();
            }catch (Exception e) {
                throw new RuntimeException("CRON表达式解析异常");
            }
        }
        // 固定频率
        if (triggerType == TriggerType.SPEED.getCode()) {
            return dateTime + speedS * 1000;
        }
        // 固定延时
        if (triggerType == TriggerType.DELAYED.getCode()) {
            return dateTime + delayedS * 1000;
        }

        return null;
    }


    public String getUid() {
        String jobId = String.valueOf(String.valueOf(this.getId()));
        if (this.getInstanceId() != null) {
            jobId = jobId + "_" + this.getInstanceId();
        }
        return jobId;
    }

    /**
     * 前置推入
     * @return boolean
     */
    public boolean prePush() {
        return !Objects.equals(this.getTriggerType(), TriggerType.DELAYED.getCode());
    }

    /**
     * 后置推入
     * @return boolean
     */
    public boolean postPush() {
        return !prePush();
    }

}
