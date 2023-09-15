package com.github.vizaizai.common.model;


import java.io.Serializable;

/**
 * 任务执行
 * @author liaochongwei
 * @date 2023/6/1 14:49
 */
public class JobRunParam implements Serializable,Comparable<JobRunParam>{
    /**
     * 数据id
     */
    private Long id;
    /**
     * 任务编码
     */
    private String jobCode;
    /**
     * 任务参数
     */
    private String jobParam;
    /**
     * 触发时间
     */
    private long triggerTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getJobParam() {
        return jobParam;
    }

    public void setJobParam(String jobParam) {
        this.jobParam = jobParam;
    }

    public long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(long triggerTime) {
        this.triggerTime = triggerTime;
    }

    @Override
    public int compareTo(JobRunParam o) {
        return (int)(this.triggerTime - o.triggerTime);
    }
}
