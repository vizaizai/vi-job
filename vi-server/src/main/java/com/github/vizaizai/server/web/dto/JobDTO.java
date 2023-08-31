package com.github.vizaizai.server.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.vizaizai.server.constant.Commons;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务信息-DTO
 * @author liaochongwei
 * @date 2023/5/18 17:11
 */
@Data
public class JobDTO {

    /**
     * id
     */
    private Long id;
    /**
     * 任务编码
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
     * 执行器id
     */
    private String workerName;

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
     * 任务状态 0-停止 1-运行中
     */
    private Integer status;
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
     * 创建时间
     */
    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime createTime;
    /**
     * 下次触发时间
     */
    private Long nextTriggerTime;
    /**
     * 触发时间
     */
    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime nextTriggerTime0;
}
