package com.github.vizaizai.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.dao.RegistryMapper;
import com.github.vizaizai.server.dao.WorkerMapper;
import com.github.vizaizai.server.dao.dataobject.RegistryDO;
import com.github.vizaizai.server.dao.dataobject.WorkerDO;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.utils.UserUtils;
import com.github.vizaizai.server.web.co.RegisterCO;
import com.github.vizaizai.server.web.co.WorkerQueryCO;
import com.github.vizaizai.server.web.co.WorkerUpdateCO;
import com.github.vizaizai.server.web.dto.RegistryDTO;
import com.github.vizaizai.server.web.dto.WorkerDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liaochongwei
 * @date 2023/5/8 14:35
 */
@Service
@Slf4j
public class WorkerServiceImpl implements WorkerService {

    @Resource
    private WorkerMapper workerMapper;

    @Resource
    private RegistryMapper registryMapper;

    @Transactional
    @Override
    public Result<Void> saveOrUpdateWorker(WorkerUpdateCO updateCO) {
        WorkerDO worker = BeanUtils.toBean(updateCO, WorkerDO::new);
        if (worker.getId() != null) {
            // 应用名称禁止修改
            worker.setAppName(null);
            workerMapper.updateById(worker);
            return Result.ok("操作成功");
        }
        WorkerDO workerTemp = workerMapper.selectOne(Wrappers.<WorkerDO>lambdaQuery().eq(WorkerDO::getAppName, updateCO.getAppName()));
        if (workerTemp != null) {
            return Result.handleFailure("该执行器已存在");
        }
        if (StringUtils.isBlank(worker.getName()) ) {
            return Result.handleFailure("执行器名称不能为空");
        }
        if (StringUtils.isBlank(worker.getAppName())) {
            return Result.handleFailure("应用名称不能为空");
        }
        worker.setCreater(UserUtils.getUserName());
        workerMapper.insert(worker);
        return Result.ok("操作成功");
    }

    @Override
    public Result<IPage<WorkerDTO>> pageWorkers(WorkerQueryCO queryCO) {
        LambdaQueryWrapper<WorkerDO> wrapper = Wrappers.<WorkerDO>lambdaQuery()
                .eq(queryCO.getAppName() != null, WorkerDO::getAppName, queryCO.getAppName())
                .orderByDesc(WorkerDO::getCreateTime);
        Page<WorkerDO> workerPage = workerMapper.selectPage(queryCO.toPage(), wrapper);
        return Result.handleSuccess(BeanUtils.toPageBean(workerPage,WorkerDTO::new));
    }

    @Override
    public Result<Void> removeWorker(Integer id) {
        // 检查任务(存在任务无法删除)
        int count = workerMapper.deleteById(id);
        if (count == 0) {
            return Result.handleFailure("删除执行器失败");
        }
        return Result.ok("移除执行器成功");
    }

    @Override
    public Result<List<RegistryDTO>> listWorkerNodes(Integer workerId) {
        List<RegistryDO> registries = registryMapper.selectList(Wrappers.<RegistryDO>lambdaQuery().eq(RegistryDO::getWorkerId, workerId));
        return Result.handleSuccess(BeanUtils.toBeans(registries, RegistryDTO::new));

    }

    @Transactional
    @Override
    public Result<Void> register(RegisterCO registerCO) {
        if (registerCO.getAddress().split(":").length != 2) {
            return Result.handleFailure("注册地址格式错误（ip:port）");
        }
        // 查询执行器
        WorkerDO worker = workerMapper.selectOne(Wrappers.<WorkerDO>lambdaQuery().eq(WorkerDO::getAppName, registerCO.getAppName()));
        if (worker == null) {
            return Result.handleFailure("执行器[" + registerCO.getAppName() + "]未创建，注册失败");
        }
        //是否存在
        List<RegistryDO> registries = registryMapper.selectList(Wrappers.<RegistryDO>lambdaQuery().eq(RegistryDO::getWorkerId, worker.getId())
                .eq(RegistryDO::getAddress, registerCO.getAddress()));

        if (Utils.isEmpty(registries)) {
            RegistryDO registryDO = BeanUtils.toBean(registerCO, RegistryDO::new);
            registryDO.setWorkerId(worker.getId());
            registryMapper.insert(registryDO);
        }

        return Result.ok("注册成功");
    }

    @Transactional
    @Override
    public Result<Void> unregister(RegisterCO registerCO) {
        if (registerCO.getAddress().split(":").length != 2) {
            return Result.handleFailure("地址格式错误（ip:port）");
        }
        // 查询执行器
        WorkerDO worker = workerMapper.selectOne(Wrappers.<WorkerDO>lambdaQuery().eq(WorkerDO::getAppName, registerCO.getAppName()));
        if (worker == null) {
            return Result.handleFailure("执行器[" + registerCO.getAppName() + "]未创建，注册失败");
        }
        registryMapper.delete(Wrappers.<RegistryDO>lambdaQuery().eq(RegistryDO::getWorkerId, worker.getId())
                .eq(RegistryDO::getAddress, registerCO.getAddress()));
        return Result.ok("注册移除成功");
    }

    @Override
    public List<String> getWorkerAddressList(Integer workerId) {
        List<RegistryDO> registries = registryMapper.selectList(Wrappers.<RegistryDO>lambdaQuery().eq(RegistryDO::getWorkerId, workerId));
        if (Utils.isEmpty(registries)) {
            return Collections.emptyList();
        }
        return registries.stream().map(RegistryDO::getAddress).collect(Collectors.toList());
    }
}
