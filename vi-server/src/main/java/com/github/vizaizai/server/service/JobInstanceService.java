package com.github.vizaizai.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.vizaizai.common.model.LogInfo;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.dao.dataobject.JobInstanceDO;
import com.github.vizaizai.server.web.co.JobInstanceQueryCO;
import com.github.vizaizai.server.web.co.LogQueryCO;
import com.github.vizaizai.server.web.dto.JobInstanceDTO;

import java.util.List;

/**
 * 任务实例接口服务
 * @author liaochongwei
 * @date 2023/6/14 16:01
 */
public interface JobInstanceService {
    /**
     * 分页查询任务实例
     * @param queryCO
     * @return
     */
    Result<IPage<JobInstanceDTO>> pageJobInstances(JobInstanceQueryCO queryCO);

    /**
     * 查询执行日志
     * @param logQueryCO
     * @return
     */
    Result<LogInfo> getLog(LogQueryCO logQueryCO);

    /**
     * 取消执行，只能取消已调度-待执行任务
     * @param id
     * @return
     */
    Result<Void> cancel(Long id);

    /**
     * 删除任务实例
     * @param id
     * @return
     */
    Result<Void> remove(Long id);

    /**
     * 批量删除任务实例
     * @return
     */
    int batchRemove();

    /**
     * 查询等待中调度的任务实例
     * @param maxTime 最大时间
     * @return
     */
    List<JobInstanceDO> listWaitingInstances(long maxTime);

}
