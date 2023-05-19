package com.github.vizaizai.server.raft.processor;

import java.io.Serializable;

/**
 * 任务计划操作
 * @author liaochongwei
 * @date 2023/5/18 14:18
 */
public class JobPlanOpt implements Serializable {
    /**
     * put操作
     */
    public static final byte  PUT = 0x01;
    /**
     * remove操作
     */
    public static final byte  RM = 0x02;
    /**
     * 操作符
     */
    private byte op;
    /**
     * 任务id
     */
    private Integer jobId;
    /**
     * 调度地址
     */
    private String address;

    public static JobPlanOpt createPut(Integer jobId, String address) {
        JobPlanOpt opt = new JobPlanOpt();
        opt.setOp(PUT);
        opt.setJobId(jobId);
        opt.setAddress(address);
        return opt;
    }

    public static JobPlanOpt createRm(Integer jobId) {
        JobPlanOpt opt = new JobPlanOpt();
        opt.setOp(RM);
        opt.setJobId(jobId);
        return opt;
    }

    public byte getOp() {
        return op;
    }

    public void setOp(byte op) {
        this.op = op;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
