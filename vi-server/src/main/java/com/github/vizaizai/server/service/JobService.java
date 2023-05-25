package com.github.vizaizai.server.service;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.web.co.JobStatusUpdateCO;
import com.github.vizaizai.server.web.co.JobUpdateCO;

/**
 * 任务接口
 * @author liaochongwei
 * @date 2023/5/18 18:25
 */
public interface JobService {

    /**
     * 新增任务
     * @param jobUpdateCO
     * @return
     */
    Result<Void> addJob(JobUpdateCO jobUpdateCO);

    /**
     * 更新任务状态
     * @param jobStatusUpdateCO
     * @return
     */
    Result<Void> updateJobStatus(JobStatusUpdateCO jobStatusUpdateCO);

    /**
     * 执行任务
     * @param job 任务
     */
    void invoke(Job job);
}
