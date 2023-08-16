package com.github.vizaizai.common.model;


import java.io.Serializable;

/**
 * 状态上报
 * @author liaochongwei
 * @date 2023/6/1 14:49
 */
public class StatusReportParam implements Serializable {
    /**
     * 任务id
     */
    private Long jobId;
    /**
     * 调度id
     */
    private Long dispatchId;
    /**
     * 触发类型
     */
    private Integer triggerType;
    /**
     * 执行状态 0-失败 1-执行中 2-执行成功 3-执行超时 4-取消
     */
    private Integer executeStatus;
    /**
     * 执行开始时间
     */
    private long executeStartTime;
    /**
     * 执行结束时间
     */
    private long executeEndTime;
    /**
     * 执行次数
     */
    private Integer execCount;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(Long dispatchId) {
        this.dispatchId = dispatchId;
    }

    public Integer getExecuteStatus() {
        return executeStatus;
    }

    public void setExecuteStatus(Integer executeStatus) {
        this.executeStatus = executeStatus;
    }

    public long getExecuteStartTime() {
        return executeStartTime;
    }

    public void setExecuteStartTime(long executeStartTime) {
        this.executeStartTime = executeStartTime;
    }

    public long getExecuteEndTime() {
        return executeEndTime;
    }

    public void setExecuteEndTime(long executeEndTime) {
        this.executeEndTime = executeEndTime;
    }

    public Integer getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(Integer triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getExecCount() {
        return execCount;
    }

    public void setExecCount(Integer execCount) {
        this.execCount = execCount;
    }
}
