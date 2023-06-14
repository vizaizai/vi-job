package com.github.vizaizai.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.co.DispatchLogQueryCO;
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
}
