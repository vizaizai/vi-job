package com.github.vizaizai.server.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 任务分组信息
 * @author liaochongwei
 * @date 2023/6/19 17:22
 */
@Data
public class JobGroup implements Serializable {
    /**
     * 节点地址
     */
    private String address;
    /**
     * 任务列表
     */
    private Set<Long> jobSet = new HashSet<>();

    public JobGroup(String address) {
        this.address = address;
    }
    public JobGroup(String address, Long jobId) {
        this.address = address;
        jobSet.add(jobId);
    }

    public void putJob(Long jobId) {
        this.jobSet.add(jobId);
    }

    public void removeJob(Long jobId) {
        this.jobSet.remove(jobId);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Set<Long> getJobSet() {
        return jobSet;
    }

    public void setJobSet(Set<Long> jobSet) {
        this.jobSet = jobSet;
    }
}
