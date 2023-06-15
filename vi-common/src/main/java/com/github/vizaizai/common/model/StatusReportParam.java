package com.github.vizaizai.common.model;


/**
 * 状态上报
 * @author liaochongwei
 * @date 2023/6/1 14:49
 */
public class StatusReportParam {
    /**
     * 任务id
     */
    private Long jobId;
    /**
     * 调度id
     */
    private Long dispatchId;
    /**
     * 执行状态 0-失败 1-执行中 2-执行成功 3-执行成功（超时） 4-超时中断 5-主动中断
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
}
