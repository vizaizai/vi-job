package com.github.vizaizai.server.service;

import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.co.RegisterCO;
import com.github.vizaizai.server.web.co.WorkerUpdateCO;

/**
 * 执行器业务接口
 * @author liaochongwei
 * @date 2023/5/8 14:15
 */
public interface WorkerService {

    /**
     * 新增编辑执行器
     * @param updateCO
     * @return
     */
    Result<Void> saveOrUpdateWorker(WorkerUpdateCO updateCO);

    /**
     * 移除执行器
     * @param id
     * @return
     */
    Result<Void> removeWorker(Integer id);
    /**
     * 注册执行器
     * @param registerCO
     */
    Result<Void> register(RegisterCO registerCO);

    /**
     * 移除执行器
     * @param registerCO
     * @return
     */
    Result<Void> unregister(RegisterCO registerCO);

    
}
