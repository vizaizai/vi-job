package com.github.vizaizai.server.entity;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.github.vizaizai.retry.mode.CronSequenceGenerator;
import com.github.vizaizai.server.constant.TriggerType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 执行器id
     */
    private String workerId;

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
    private Integer speed;
    /**
     * 延时
     */
    private Integer delayed;
    /**
     * 下一次触发时间
     */
    private LocalDateTime nextTriggerTime;
    /**
     * 上一次触发时间
     */
    private LocalDateTime lastTriggerTime;
    /**
     * 上一次执行结束时间
     */
    private LocalDateTime lastExecuteEndTime;
    /**
     * 执行器地址列表
     */
    private List<String> workerAddressList;
    /**
     * 计算下一次触发时间
     * @return 时间戳
     */
    public Long calculateNextTriggerTime() {
        if (triggerType == TriggerType.NON.getCode()) {
            return null;
        }
        // cron
        if (triggerType == TriggerType.CRON.getCode()){
            final CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(cron);
            Date next = cronSequenceGenerator.next(new Date());
            return next.getTime();
        }
        // 固定频率
        if (triggerType == TriggerType.SPEED.getCode()) {
            if (lastTriggerTime == null) {
                lastTriggerTime = LocalDateTimeUtil.now();
            }
            return LocalDateTimeUtil.toEpochMilli(lastTriggerTime) + speed;
        }
        // 固定延时，需要基于上一次执行完成时间
        if (triggerType == TriggerType.DELAYED.getCode()) {
            // 首次触发
            if (lastTriggerTime == null) {
                lastTriggerTime = LocalDateTimeUtil.now();
                return LocalDateTimeUtil.toEpochMilli(lastTriggerTime) + delayed;
            }
            // 上一次未执行完成
            if (lastExecuteEndTime == null) {
                return null;
            }
            return LocalDateTimeUtil.toEpochMilli(lastExecuteEndTime) + delayed;
        }

        return null;
    }

}
