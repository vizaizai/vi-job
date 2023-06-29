package com.github.vizaizai.server.raft.processor;

import java.io.Serializable;

/**
 * 分配命令
 * @author liaochongwei
 * @date 2023/5/18 14:18
 */
public class AllocationCommand implements Serializable {
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
    private Long jobId;
    /**
     * 调度地址
     */
    private String address;

    public static AllocationCommand createPut(Long jobId, String address) {
        AllocationCommand opt = new AllocationCommand();
        opt.setOp(PUT);
        opt.setJobId(jobId);
        opt.setAddress(address);
        return opt;
    }

    public static AllocationCommand createRm(Long jobId) {
        AllocationCommand opt = new AllocationCommand();
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

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
