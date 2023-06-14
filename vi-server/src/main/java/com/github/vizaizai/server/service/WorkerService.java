package com.github.vizaizai.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.web.co.RegisterCO;
import com.github.vizaizai.server.web.co.WorkerQueryCO;
import com.github.vizaizai.server.web.co.WorkerUpdateCO;
import com.github.vizaizai.server.web.dto.RegistryDTO;
import com.github.vizaizai.server.web.dto.WorkerDTO;

import java.util.List;
import java.util.Set;

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
     * 分页查询workers
     * @param queryCO
     * @return
     */
    Result<IPage<WorkerDTO>> pageWorkers(WorkerQueryCO queryCO);
    /**
     * 移除执行器
     * @param id
     * @return
     */
    Result<Void> removeWorker(Integer id);

    /**
     * 根据执行器id查询节点列表
     * @param workerId 执行器id
     * @return
     */
    Result<List<RegistryDTO>> listWorkerNodes(Integer workerId);
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

    /**
     * 获取在线地址列表
     * @param workerId
     * @return
     */
    List<String> getWorkerAddressList(Integer workerId);

    List<WorkerDTO> listByIds(Set<Integer> ids);
    
}
