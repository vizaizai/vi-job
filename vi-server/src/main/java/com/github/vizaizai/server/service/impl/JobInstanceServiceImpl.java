package com.github.vizaizai.server.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.contants.ExecuteStatus;
import com.github.vizaizai.common.model.*;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.config.ServerProperties;
import com.github.vizaizai.server.constant.DispatchStatus;
import com.github.vizaizai.server.dao.JobInstanceMapper;
import com.github.vizaizai.server.dao.JobMapper;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.dao.dataobject.JobInstanceDO;
import com.github.vizaizai.server.service.JobInstanceService;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.utils.RpcUtils;
import com.github.vizaizai.server.web.co.JobInstanceQueryCO;
import com.github.vizaizai.server.web.co.LogQueryCO;
import com.github.vizaizai.server.web.dto.JobInstanceDTO;
import com.github.vizaizai.server.web.dto.WorkerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author liaochongwei
 * @date 2023/6/14 16:18
 */
@Slf4j
@Service
public class JobInstanceServiceImpl implements JobInstanceService {

    @Resource
    private JobInstanceMapper jobInstanceMapper;
    @Resource
    private JobMapper jobMapper;
    @Resource
    private WorkerService workerService;
    @Resource
    private ServerProperties serverProperties;

    @Override
    public Result<IPage<JobInstanceDTO>> pageJobInstances(JobInstanceQueryCO queryCO) {
        LambdaQueryWrapper<JobInstanceDO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(queryCO.getJobId() != null, JobInstanceDO::getJobId, queryCO.getJobId())
                .eq(queryCO.getWorkerId() != null, JobInstanceDO::getWorkerId, queryCO.getWorkerId())
                .eq(queryCO.getDispatchStatus() != null, JobInstanceDO::getDispatchStatus, queryCO.getDispatchStatus())
                .in(queryCO.getDispatchStatus() == null, JobInstanceDO::getDispatchStatus, DispatchStatus.codes())
                .eq(queryCO.getExecuteStatus() != null, JobInstanceDO::getExecuteStatus, queryCO.getExecuteStatus())
                .eq( JobInstanceDO::getPid, 0)
                .between(queryCO.getTriggerStartTime() != null && queryCO.getTriggerEndTime() != null,
                        JobInstanceDO::getTriggerTime, queryCO.getTriggerStartTime(), queryCO.getTriggerEndTime())
                .orderByDesc(JobInstanceDO::getTriggerTime);

        IPage<JobInstanceDTO> jobInstancePage = BeanUtils.toPageBean(jobInstanceMapper.selectPage(queryCO.toPage(), queryWrapper), JobInstanceDTO::new);
        if (Utils.isNotEmpty(jobInstancePage.getRecords())) {
            List<JobInstanceDTO> records = jobInstancePage.getRecords();
            Set<Integer> set = records.stream().map(JobInstanceDTO::getWorkerId).collect(Collectors.toSet());
            Map<Integer, String> map = workerService.listByIds(set).stream().collect(Collectors.toMap(WorkerDTO::getId, WorkerDTO::getName));
            for (JobInstanceDTO record : records) {
                JobDO jobDO = jobMapper.selectById(record.getJobId());
                if (Objects.nonNull(jobDO)) {
                    record.setJobName(jobDO.getName());
                    record.setWorkerName(map.get(record.getWorkerId()));
                }
                if (Objects.equals(record.getExecuteStatus(), ExecuteStatus.ING.getCode())) {
                    record.setExecStatus(this.getExecStatus(record.getWorkerAddress(), record.getJobId(), record.getId()));
                }
                List<JobInstanceDTO> children = BeanUtils.toBeans(this.listByPid(record.getId()), JobInstanceDTO::new);
                record.setChildren(children);
                if (Utils.isNotEmpty(children)) {
                    for (JobInstanceDTO child : children) {
                        child.setJobName(record.getJobName());
                        child.setWorkerName(record.getWorkerName());
                    }
                }

            }
        }
        return Result.handleSuccess(jobInstancePage);
    }

    @Override
    public Result<LogInfo> getLog(LogQueryCO logQueryCO) {
        JobInstanceDO jobInstance = jobInstanceMapper.selectById(logQueryCO.getId());
        if (jobInstance == null) {
            return Result.handleFailure("任务实例不存在");
        }
        if (!Objects.equals(DispatchStatus.OK.getCode(), jobInstance.getDispatchStatus())) {
            return Result.handleFailure("当前调度状态不支持查询执行日志");
        }
        LogQueryParam param = new LogQueryParam();
        param.setJobId(jobInstance.getJobId());
        param.setLogId(jobInstance.getId());
        param.setTriggerTime(LocalDateTimeUtil.toEpochMilli(jobInstance.getTriggerTime()));
        param.setStartPos(logQueryCO.getStartPos());
        param.setMaxLines(logQueryCO.getMaxLines());

        RpcResponse response = RpcUtils.call(jobInstance.getWorkerAddress(), BizCode.LOG, param);
        if (!response.getSuccess()) {
            log.error("查询执行日志错误: {}", response.getMsg());
            return Result.handleSuccess(null);
        }
        LogInfo logInfo = (LogInfo) response.getResult();
        return Result.handleSuccess(logInfo);
    }

    @Transactional
    @Override
    public Result<Void> cancel(Long id) {
        JobInstanceDO jobInstance = jobInstanceMapper.selectById(id);
        if (jobInstance == null) {
            return Result.handleFailure("任务实例不存在");
        }
        if (!Objects.equals(ExecuteStatus.ING.getCode(), jobInstance.getExecuteStatus())) {
            return Result.handleFailure("当前执行状态无法取消执行");
        }
        // 执行取消
        boolean canceled = this.cancel(jobInstance.getWorkerAddress(), jobInstance.getJobId(), jobInstance.getId());
        if (canceled) {
            JobInstanceDO jobInstanceForUpdate = new JobInstanceDO();
            jobInstanceForUpdate.setId(id);
            jobInstanceForUpdate.setExecuteStatus(ExecuteStatus.EXEC_CANCEL.getCode());
            jobInstanceMapper.updateById(jobInstanceForUpdate);
            return Result.ok("取消成功");
        }
        return Result.handleFailure("取消失败，任务正在执行或已执行完毕");
    }

    @Transactional
    @Override
    public Result<Void> remove(Long id) {
        jobInstanceMapper.deleteById(id);
        return Result.ok();
    }

    @Override
    public int batchRemove() {
        return jobInstanceMapper.delete(Wrappers.<JobInstanceDO>lambdaQuery()
                .lt(JobInstanceDO::getExpectedDeleteTime, LocalDateTimeUtil.now()));
    }

    @Override
    public List<JobInstanceDO> listWaitingInstances(long maxTime) {
        Wrapper<JobInstanceDO> queryWrapper = Wrappers.<JobInstanceDO>lambdaQuery()
                .eq(JobInstanceDO::getDispatchStatus, DispatchStatus.WAIT.getCode())
                .eq(JobInstanceDO::getPid, 0)
                .le(JobInstanceDO::getTriggerTime, LocalDateTimeUtil.of(maxTime))
                .orderByAsc(JobInstanceDO::getTriggerTime);
        Page<JobInstanceDO> jobPage = jobInstanceMapper.selectPage(new Page<>(1, serverProperties.getTriggerMaximum()), queryWrapper);
        return jobPage.getRecords();
    }

    /**
     * 取消执行
     * @param address 地址
     * @param jobId 任务id
     * @param jobInstanceId 调度id
     */
    private boolean cancel(String address, Long jobId, Long jobInstanceId) {
        ExecCancelParam param = new ExecCancelParam();
        param.setJobId(jobId);
        param.setJobInstanceId(jobInstanceId);
        RpcResponse response = RpcUtils.call(address, BizCode.CANCEL, param);
        if (response.getSuccess()) {
            return (boolean) response.getResult();
        }
        return false;
    }
    /**
     * 查询执行状态
     * @param address 地址
     * @param jobId 任务id
     * @param jobInstanceId 任务实例id
     * @return 状态
     */
    private Integer getExecStatus(String address, Long jobId, Long jobInstanceId) {
        ExecStatusQueryParam param = new ExecStatusQueryParam();
        param.setJobId(jobId);
        param.setJobInstanceId(jobInstanceId);
        RpcResponse response = RpcUtils.call(address, BizCode.STATUS, param);
        if (response.getSuccess()) {
            return (Integer) response.getResult();
        }
        return null;
    }

    private List<JobInstanceDO> listByPid(Long pid) {
        return jobInstanceMapper.selectList(Wrappers.<JobInstanceDO>lambdaQuery().eq(JobInstanceDO::getPid, pid));
    }
}
