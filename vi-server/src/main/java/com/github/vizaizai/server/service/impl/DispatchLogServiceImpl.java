package com.github.vizaizai.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.server.constant.DispatchStatus;
import com.github.vizaizai.server.dao.DispatchLogMapper;
import com.github.vizaizai.server.dao.JobMapper;
import com.github.vizaizai.server.dao.dataobject.DispatchLogDO;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.service.DispatchLogService;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.web.co.DispatchLogQueryCO;
import com.github.vizaizai.server.web.dto.DispatchLogDTO;
import com.github.vizaizai.server.web.dto.WorkerDTO;
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
}
