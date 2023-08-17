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
     * 任务实例id
     */
    private Long jobInstanceId;

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }
}
