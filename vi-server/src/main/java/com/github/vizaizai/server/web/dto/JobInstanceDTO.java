package com.github.vizaizai.server.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.vizaizai.server.constant.Commons;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务实例-DTO
 * @author liaochongwei
 * @date 2023/5/18 19:51
 */
@Data
public class JobInstanceDTO {
    /**
     * id
     */
    private Long id;
    /**
     * 父id
     */
    private Long pid;
    /**
     * 任务id
     */
    private Long jobId;
    /**
     * 任务名称
     */
    private String jobName;
    /**
     * 任务参数
     */
    private String jobParam;

    /**
     * 执行器id
     */
    private Integer workerId;
    /**
     * 执行器名称
     */
    private String workerName;

    /**
     * 执行器地址
     */
    private String workerAddress;

    /**
     * 调度状态
     */
    private Integer dispatchStatus;

    /**
     * 执行状态 0-执行失败 1-执行中 2-执行成功 3-执行超时 4-取消
     */
    private Integer executeStatus;
    /**
     * 错误消息
     */
    private String errorMsg;
    /**
     * 处理器类型 1-Bean 2-HTTP
     */
    private Integer processorType;
    /**
     * 处理器
     */
    private String processor;
    /**
     * 触发时间
     */
    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime triggerTime;
    /**
     * 执行开始时间
     */
    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime executeStartTime;
    /**
     * 执行结束时间
     */
    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime executeEndTime;
    /**
     * 执行次数
     */
    private Integer execCount;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = Commons.DT_PATTERN)
    private LocalDateTime createTime;

    /**
     * 执行状态 0-未知 1-执行中 2-待执行
     */
    private Integer execStatus;
    /**
     * 子实例
     */
    private List<JobInstanceDTO> children;
}
