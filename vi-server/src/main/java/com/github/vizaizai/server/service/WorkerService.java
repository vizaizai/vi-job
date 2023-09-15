package com.github.vizaizai.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.server.dao.dataobject.RegistryDO;
import com.github.vizaizai.server.web.co.RegisterCO;
import com.github.vizaizai.server.web.co.WorkerQueryCO;
import com.github.vizaizai.server.web.co.WorkerUpdateCO;
import com.github.vizaizai.server.web.dto.PingResult;
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
    Result<String> register(RegisterCO registerCO);

    /**
     * 移除执行器
     * @param registerCO
     * @return
     */
    Result<Void> unregister(RegisterCO registerCO);

    /**
     * 获取在线地址列表
     * @param workerId 执行器id
     * @return
     */
    List<String> getWorkerAddressList(Integer workerId);

    /**
     * 向worker发送心跳指令
     * @param address 地址
     * @return PingResult
     */
    PingResult ping(String address);

    List<WorkerDTO> listByIds(Set<Integer> ids);

    /**
     * 移除注册表
     * @param workerId 执行器id
     * @param address 地址
     */
    void removeRegistry(Integer workerId, String address);

    /**
     * 查询失效的注册表
     * @return
     */
    List<RegistryDO> listDeadRegistries();
    
}
