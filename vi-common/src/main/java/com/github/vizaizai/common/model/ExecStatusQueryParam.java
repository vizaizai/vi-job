package com.github.vizaizai.common.model;

import java.io.Serializable;

/**
 * 执行状态查询参数
 * @author liaochongwei
 * @date 2023/7/10 16:35
 */
public class ExecStatusQueryParam implements Serializable {
    /**
     * 任务id
     */
    private long jobId;
    /**
     * 任务调度id
     */
    private Long jobDispatchId;

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public Long getJobDispatchId() {
        return jobDispatchId;
    }

    public void setJobDispatchId(Long jobDispatchId) {
        this.jobDispatchId = jobDispatchId;
    }
}
