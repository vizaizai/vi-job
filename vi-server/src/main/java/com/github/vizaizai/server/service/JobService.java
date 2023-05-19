package com.github.vizaizai.server.service;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.co.JobAddCO;

/**
 * 任务接口
 * @author liaochongwei
 * @date 2023/5/18 18:25
 */
public interface JobService {

    /**
     * 新增任务
     * @param jobAddCO
     * @return
     */
    Result<Void> addJob(JobAddCO jobAddCO);
}
