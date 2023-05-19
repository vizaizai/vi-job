package com.github.vizaizai.server.web.co;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 任务添加-CO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class JobAddCO {
    /**
     * 任务名称
     */
    @NotEmpty(message = "任务名称必须")
    private String name;

    /**
     * 执行器id
     */
    @NotEmpty(message = "执行器id必须")
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
    @NotNull(message = "处理器类型必须")
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
    @NotNull(message = "触发类型必须")
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
     * 新建人
     */
    private String creater;
}
