package com.github.vizaizai.server.service;

import com.github.vizaizai.server.raft.processor.AllocationClosure;

/**
 * 任务分配业务类
 * @author liaochongwei
 * @date 2023/6/19 17:18
 */
public interface JobAllocationService {
    /**
     * put操作
     * @param jobId 任务id
     */
    void put(Long jobId, AllocationClosure closure);
    /**
     * rm操作
     * @param jobId id
     */
    void rm(Long jobId, AllocationClosure closure);
    
    /**
     * 执行put
     * @param jobId 任务id
     * @param address 节点地址
     */
    void doPut(Long jobId, String address);

    /**
     * 执行rm
     * @param jobId 任务id
     */
    void doRm(Long jobId);

    /**
     * get操作
     * @param jobId 任务id
     * @return 节点地址
     */
    String get(Long jobId);

    /**
     * 获取序列化数据
     * @return
     */
    byte[] getData();

    /**
     * 初始化
     * @param data
     */
    void init(byte[] data);
}
