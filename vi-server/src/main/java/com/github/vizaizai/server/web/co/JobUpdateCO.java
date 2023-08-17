package com.github.vizaizai.server.web.co;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.vizaizai.server.constant.Commons;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 任务更新-CO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class JobUpdateCO {
    /**
     * 数据id
     */
    private Long id;
    /**
     * 任务名称
     */
    @NotEmpty(message = "任务名称必须")
    private String name;

    /**
     * 执行器id
     */
    @NotNull(message = "执行器id必须")
    private Integer workerId;

    /**
     * 生命周期开始
     */
    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime startTime;

    /**
     * 生命周期结束
     */
    @JsonFormat(pattern = Commons.DT_PATTERN)
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
    private Integer speedS;
    /**
     * 延时
     */
    private Integer delayedS;
    /**
     * 路由策略
     */
    @NotNull(message = "路由策略必须")
    private Integer routeType;
    /**
     * 任务失败重试次数
     */
    @NotNull(message = "任务失败重试次数必须")
    @Range(max = 10, message = "重试次数最大为10")
    private Integer retryCount;
    /**
     * 任务超时时间
     */
    @Range(min = 0, message = "任务超时时间参数错误")
    private Integer timeoutS;
    /**
     * 最大等待数量
     */
    private Integer maxWaitNum;
    /**
     * 任务实例自动删除时间（小时）
     */
    @Range(min = 1, message = "自动删除时间最少1小时")
    private Integer logAutoDelHours;
    /**
     * 新建人
     */
    private String creater;

}
