package com.github.vizaizai.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.JobRunParam;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.entity.JobCache;
import com.github.vizaizai.server.web.co.*;
import com.github.vizaizai.server.web.dto.JobDTO;
import com.github.vizaizai.server.web.dto.JobRunDTO;

import java.util.List;

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
     * 更新任务
     * @param jobUpdateCO
     * @return
     */
    Result<Void> updateJob(JobUpdateCO jobUpdateCO);

    /**
     * 删除任务
     * @param id 任务id
     * @return
     */
    Result<Void> removeJob(Long id);

    /**
     * 分页查询任务
     * @param jobQueryCO
     * @return
     */
    Result<IPage<JobDTO>> pageJobs(JobQueryCO jobQueryCO);
    /**
     * 更新任务状态
     * @param jobStatusUpdateCO
     * @return
     */
    Result<Void> updateJobStatus(JobStatusUpdateCO jobStatusUpdateCO);
    /**
     * 参数运行
     * @param jobRunCO
     * @return
     */
    Result<JobRunDTO> run(JobRunCO jobRunCO);

    /**
     * 参数运行
     * @param jobRunParam
     * @return
     */
    Result<Long> run(JobRunParam jobRunParam);
    /**
     * 状态上报
     * @param statusReportList
     * @return
     */
    Result<Void> statusReport(List<StatusReportCO> statusReportList);
    /**
     * 查询所有等待触发的任务
     * @param maxTime 最大时间
     * @return
     */
    List<JobDO> listWaitingJobs(long maxTime);
    /**
     * 执行任务
     * @param job 任务
     */
    void invoke(Job job);
    /**
     * 刷新触发时间
     * @param jobId 任务id
     * @param lastTriggerTime 上次触发时间
     * @param nextTriggerTime 下次触发时间
     */
    boolean refreshTriggerTime(Long jobId, Long lastTriggerTime, Long nextTriggerTime);

    /**
     * 停止任务
     * @param jobId 任务id
     */
    void stop(Long jobId);
    /**
     * 批量id查询
     * @param ids
     * @return
     */
    List<JobDO> listByIds(List<Long> ids);

    /**
     * 获取任务缓存
     * @param id 任务id
     * @return JobCache
     */
    JobCache getJobCache(Long id);
}
