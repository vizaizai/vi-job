package com.github.vizaizai.common.model;

import java.io.Serializable;

/**
 * 任务上下文
 * @author liaochongwei
 * @date 2023/4/23 10:18
 */
public class TaskContext implements Serializable {

    /**
     * 任务id
     */
    private Long jobId;
    /**
     * 任务名称
     */
    private String jobName;
    /**
     * 任务调度id
     */
    private String jobDispatchId;
    /**
     * 任务参数
     */
    private String jobParams;
    /**
     * 任务超时时间（单位：秒）
     */
    private Integer executeTimeout;
    /**
     * 任务超时处理策略 1-标记 2-中断
     */
    private Integer timeoutHandleType = 1;




    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDispatchId() {
        return jobDispatchId;
    }

    public void setJobDispatchId(String jobDispatchId) {
        this.jobDispatchId = jobDispatchId;
    }

    public String getJobParams() {
        return jobParams;
    }

    public void setJobParams(String jobParams) {
        this.jobParams = jobParams;
    }

    public Integer getExecuteTimeout() {
        return executeTimeout;
    }

    public void setExecuteTimeout(Integer executeTimeout) {
        this.executeTimeout = executeTimeout;
    }

    public Integer getTimeoutHandleType() {
        return timeoutHandleType;
    }

    public void setTimeoutHandleType(Integer timeoutHandleType) {
        this.timeoutHandleType = timeoutHandleType;
    }

    @Override
    public String toString() {
        return "TaskContext{" +
                "jobId='" + jobId + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jobDispatchId='" + jobDispatchId + '\'' +
                ", jobParams='" + jobParams + '\'' +
                ", executeTimeout=" + executeTimeout +
                ", timeoutHandleType=" + timeoutHandleType +
                '}';
    }
}
