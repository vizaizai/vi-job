package com.github.vizaizai.server.web.co;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 状态上报-CO
 * @author liaochongwei
 * @date 2023/6/1 14:49
 */
@Data
public class StatusReportCO {
    @NotNull(message = "任务id")
    private Long jobId;
    /**
     * 调度id
     */
    @NotNull(message = "调度id必须")
    private String dispatchId;

    /**
     * 执行状态 0-失败 1-执行中 2-执行成功 3-执行成功（超时） 4-超时中断 5-主动中断
     */
    @NotNull(message = "执行状态必须")
    private Integer executeStatus;
    /**
     * 执行开始时间
     */
    private long executeStartTime;
    /**
     * 执行结束时间
     */
    private long executeEndTime;


}
