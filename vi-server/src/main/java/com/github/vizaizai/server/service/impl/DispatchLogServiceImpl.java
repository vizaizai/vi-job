package com.github.vizaizai.server.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.contants.ExecuteStatus;
import com.github.vizaizai.common.model.LogInfo;
import com.github.vizaizai.common.model.LogQueryParam;
import com.github.vizaizai.common.model.Result;
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

    @Override
    public Result<Void> kill(Long id) {
        DispatchLogDO dispatchLog = dispatchLogMapper.selectById(id);
        if (dispatchLog == null) {
            return Result.handleFailure("调度记录不存在");
        }
        if (!Objects.equals(ExecuteStatus.ING.getCode(), dispatchLog.getExecuteStatus())) {
            return Result.handleFailure("当前执行状态无法中断执行");
        }
        return null;
    }
}
