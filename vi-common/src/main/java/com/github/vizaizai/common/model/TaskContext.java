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
    private String jobId;
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


    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
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

    @Override
    public String toString() {
        return "{" +
                "jobId='" + jobId + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jobDispatchId='" + jobDispatchId + '\'' +
                ", jobParams='" + jobParams + '\'' +
                '}';
    }
}
