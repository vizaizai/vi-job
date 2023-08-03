package com.github.vizaizai.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.LogInfo;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.co.DispatchLogQueryCO;
import com.github.vizaizai.server.web.co.LogQueryCO;
import com.github.vizaizai.server.web.dto.DispatchLogDTO;

/**
 * 调度日志接口服务
 * @author liaochongwei
 * @date 2023/6/14 16:01
 */
public interface DispatchLogService {
    /**
     * 分页查询调度日志
     * @param queryCO
     * @return
     */
    Result<IPage<DispatchLogDTO>> pageDispatchLogs(DispatchLogQueryCO queryCO);

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
     * 删除调度日志
     * @param id
     * @return
     */
    Result<Void> remove(Long id);

    /**
     * 批量删除调度日志
     * @return
     */
    int batchRemove();
}
