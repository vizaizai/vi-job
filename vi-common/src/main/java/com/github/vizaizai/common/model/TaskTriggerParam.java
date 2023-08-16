package com.github.vizaizai.common.model;

import java.io.Serializable;

/**
 * 任务调度参数
 * @author liaochongwei
 * @date 2023/4/23 10:18
 */
public class TaskTriggerParam implements Serializable {

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
    private Long jobDispatchId;
    /**
     * 任务参数
     */
    private String jobParams;
    /**
     * 触发类型
     */
    private Integer triggerType;
    /**
     * 触发时间
     */
    private Long triggerTime;
    /**
     * 任务超时时间（单位：秒）
     */
    private Integer executeTimeout;
    /**
     * 最大等待数
     */
    private Integer maxWaitNum;
    /**
     * 执行失败重试次数
     */
    private Integer retryCount;

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

    public Long getJobDispatchId() {
        return jobDispatchId;
    }

    public void setJobDispatchId(Long jobDispatchId) {
        this.jobDispatchId = jobDispatchId;
    }

    public String getJobParams() {
        return jobParams;
    }

    public void setJobParams(String jobParams) {
        this.jobParams = jobParams;
    }

    public Integer getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(Integer triggerType) {
        this.triggerType = triggerType;
    }

    public Long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Long triggerTime) {
        this.triggerTime = triggerTime;
    }

    public Integer getExecuteTimeout() {
        return executeTimeout;
    }

    public void setExecuteTimeout(Integer executeTimeout) {
        this.executeTimeout = executeTimeout;
    }

    public Integer getMaxWaitNum() {
        return maxWaitNum;
    }

    public void setMaxWaitNum(Integer maxWaitNum) {
        this.maxWaitNum = maxWaitNum;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}
