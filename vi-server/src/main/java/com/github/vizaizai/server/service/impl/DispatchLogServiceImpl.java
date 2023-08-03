package com.github.vizaizai.server.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.contants.ExecuteStatus;
import com.github.vizaizai.common.model.*;
import com.github.vizaizai.remote.codec.RpcResponse;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.constant.DispatchStatus;
import com.github.vizaizai.server.dao.DispatchLogMapper;
import com.github.vizaizai.server.dao.JobMapper;
import com.github.vizaizai.server.dao.dataobject.DispatchLogDO;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.service.DispatchLogService;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.utils.RpcUtils;
import com.github.vizaizai.server.web.co.DispatchLogQueryCO;
import com.github.vizaizai.server.web.co.LogQueryCO;
import com.github.vizaizai.server.web.dto.DispatchLogDTO;
import com.github.vizaizai.server.web.dto.WorkerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
public class DispatchLogServiceImpl implements DispatchLogService {

    @Resource
    private DispatchLogMapper dispatchLogMapper;
    @Resource
    private JobMapper jobMapper;
    @Resource
    private WorkerService workerService;

    @Override
    public Result<IPage<DispatchLogDTO>> pageDispatchLogs(DispatchLogQueryCO queryCO) {
        LambdaQueryWrapper<DispatchLogDO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(queryCO.getJobId() != null, DispatchLogDO::getJobId, queryCO.getJobId())
                .eq(queryCO.getWorkerId() != null, DispatchLogDO::getWorkerId, queryCO.getWorkerId())
                .eq(queryCO.getDispatchStatus() != null, DispatchLogDO::getDispatchStatus, queryCO.getDispatchStatus())
                .in(queryCO.getDispatchStatus() == null, DispatchLogDO::getDispatchStatus, DispatchStatus.codes())
                .eq(queryCO.getExecuteStatus() != null, DispatchLogDO::getExecuteStatus, queryCO.getExecuteStatus())
                .between(queryCO.getTriggerStartTime() != null && queryCO.getTriggerEndTime() != null,
                        DispatchLogDO::getTriggerTime, queryCO.getTriggerStartTime(), queryCO.getTriggerEndTime())
                .orderByDesc(DispatchLogDO::getTriggerTime);

        IPage<DispatchLogDTO> dispatchLogPage = BeanUtils.toPageBean(dispatchLogMapper.selectPage(queryCO.toPage(), queryWrapper), DispatchLogDTO::new);
        if (Utils.isNotEmpty(dispatchLogPage.getRecords())) {
            List<DispatchLogDTO> records = dispatchLogPage.getRecords();
            Set<Integer> set = records.stream().map(DispatchLogDTO::getWorkerId).collect(Collectors.toSet());
            Map<Integer, String> map = workerService.listByIds(set).stream().collect(Collectors.toMap(WorkerDTO::getId, WorkerDTO::getName));
            for (DispatchLogDTO record : records) {
                JobDO jobDO = jobMapper.selectById(record.getJobId());
                if (Objects.nonNull(jobDO)) {
                    record.setJobName(jobDO.getName());
                    record.setWorkerName(map.get(record.getWorkerId()));
                }
                if (Objects.equals(record.getExecuteStatus(), ExecuteStatus.ING.getCode())) {
                    record.setExecStatus(this.getExecStatus(record.getWorkerAddress(), record.getJobId(), record.getId()));
                }
            }
        }
        return Result.handleSuccess(dispatchLogPage);
    }

    @Override
    public Result<LogInfo> getLog(LogQueryCO logQueryCO) {
        DispatchLogDO dispatchLog = dispatchLogMapper.selectById(logQueryCO.getId());
        if (dispatchLog == null) {
            return Result.handleFailure("调度记录不存在");
        }
        if (!Objects.equals(DispatchStatus.OK.getCode(), dispatchLog.getDispatchStatus())) {
            return Result.handleFailure("当前调度状态不支持查询执行日志");
        }
        LogQueryParam param = new LogQueryParam();
        param.setJobId(dispatchLog.getJobId());
        param.setLogId(dispatchLog.getId());
        param.setTriggerTime(LocalDateTimeUtil.toEpochMilli(dispatchLog.getTriggerTime()));
        param.setStartPos(logQueryCO.getStartPos());
        param.setMaxLines(logQueryCO.getMaxLines());

        RpcResponse response = RpcUtils.call(dispatchLog.getWorkerAddress(), BizCode.LOG, param);
        if (!response.getSuccess()) {
            log.error("查询执行日志错误: {}", response.getMsg());
            return Result.handleFailure(response.getMsg());
        }
        LogInfo logInfo = (LogInfo) response.getResult();
        return Result.handleSuccess(logInfo);
    }

    @Transactional
    @Override
    public Result<Void> cancel(Long id) {
        DispatchLogDO dispatchLog = dispatchLogMapper.selectById(id);
        if (dispatchLog == null) {
            return Result.handleFailure("调度记录不存在");
        }
        if (!Objects.equals(ExecuteStatus.ING.getCode(), dispatchLog.getExecuteStatus())) {
            return Result.handleFailure("当前执行状态无法取消执行");
        }
        // 执行取消
        boolean canceled = this.cancel(dispatchLog.getWorkerAddress(), dispatchLog.getJobId(), dispatchLog.getId());
        if (canceled) {
            DispatchLogDO dispatchLogForUpdate = new DispatchLogDO();
            dispatchLogForUpdate.setId(id);
            dispatchLogForUpdate.setExecuteStatus(ExecuteStatus.EXEC_CANCEL.getCode());
            dispatchLogMapper.updateById(dispatchLogForUpdate);
            return Result.ok("取消成功");
        }
        return Result.handleFailure("取消失败，任务正在执行或已执行完毕");
    }

    @Transactional
    @Override
    public Result<Void> remove(Long id) {
        dispatchLogMapper.deleteById(id);
        return Result.ok();
    }

    @Override
    public int batchRemove() {
        return dispatchLogMapper.delete(Wrappers.<DispatchLogDO>lambdaQuery()
                .lt(DispatchLogDO::getExpectedDeleteTime, LocalDateTimeUtil.now()));
    }

    /**
     * 取消执行
     * @param address 地址
     * @param jobId 任务id
     * @param dispatchLogId 调度id
     */
    private boolean cancel(String address, Long jobId, Long dispatchLogId) {
        ExecCancelParam param = new ExecCancelParam();
        param.setJobId(jobId);
        param.setJobDispatchId(dispatchLogId);
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
     * @param dispatchLogId 调度id
     * @return 状态
     */
    private Integer getExecStatus(String address, Long jobId, Long dispatchLogId) {
        ExecStatusQueryParam param = new ExecStatusQueryParam();
        param.setJobId(jobId);
        param.setJobDispatchId(dispatchLogId);
        RpcResponse response = RpcUtils.call(address, BizCode.STATUS, param);
        if (response.getSuccess()) {
            return (Integer) response.getResult();
        }
        return null;
    }
}
