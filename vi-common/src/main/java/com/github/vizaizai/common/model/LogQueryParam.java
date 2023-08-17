package com.github.vizaizai.common.model;

import java.io.Serializable;

/**
 * 日志查询参数
 * @author liaochongwei
 * @date 2023/7/10 16:35
 */
public class LogQueryParam  implements Serializable {
    /**
     * 任务id
     */
    private long jobId;
    /**
     * 日志id（任务实例id）
     */
    private long logId;
    /**
     * 日志日期
     */
    private Long triggerTime;
    /**
     * 日志起始位置
     */
    private Long startPos;
    /**
     * 日志返回最大行数
     */
    private Integer maxLines;

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public Long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Long triggerTime) {
        this.triggerTime = triggerTime;
    }

    public Long getStartPos() {
        return startPos;
    }

    public void setStartPos(Long startPos) {
        this.startPos = startPos;
    }

    public Integer getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(Integer maxLines) {
        this.maxLines = maxLines;
    }
}
