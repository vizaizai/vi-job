package com.github.vizaizai.server.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.contants.ExecuteStatus;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.common.model.TaskTriggerParam;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.retry.util.Assert;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.constant.DispatchStatus;
import com.github.vizaizai.server.constant.JobStatus;
import com.github.vizaizai.server.constant.TriggerType;
import com.github.vizaizai.server.dao.DispatchLogMapper;
import com.github.vizaizai.server.dao.JobMapper;
import com.github.vizaizai.server.dao.dataobject.DispatchLogDO;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.router.RouteType;
import com.github.vizaizai.server.service.GlobalJobGroupManager;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.timer.JobTriggerTimer;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.utils.RpcUtils;
import com.github.vizaizai.server.utils.UserUtils;
import com.github.vizaizai.server.web.co.*;
import com.github.vizaizai.server.web.dto.JobDTO;
import com.github.vizaizai.server.web.dto.WorkerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liaochongwei
 * @date 2023/5/18 19:37
 */
@Service
@Slf4j
public class JobServiceImpl implements JobService {
    @Resource
    private JobMapper jobMapper;
    @Resource
    private DispatchLogMapper dispatchLogMapper;
    @Resource
    private WorkerService workerService;
    @Resource
    private GlobalJobGroupManager globalJobGroupHandler;

    @Override
    public Result<Void> addJob(JobUpdateCO jobUpdateCO) {
        this.jobParamsCheck(jobUpdateCO);
        JobDO jobDO = BeanUtils.toBean(jobUpdateCO, JobDO::new);
        jobDO.setCreater(UserUtils.getUserName());
        jobDO.setStatus(JobStatus.STOP.getCode());
        jobDO.setNextTriggerTime(BeanUtils.toBean(jobDO, Job::new).getNextTriggerTime());
        jobMapper.insert(jobDO);
        // 全局任务分组
        globalJobGroupHandler.elect(jobDO.getId());
        return Result.ok("新增成功");
    }

    @Transactional
    @Override
    public Result<Void> updateJob(JobUpdateCO jobUpdateCO) {
        Assert.notNull(jobUpdateCO.getId(), "数据id必须");
        this.jobParamsCheck(jobUpdateCO);

        // 更新条件
        LambdaUpdateWrapper<JobDO> lambdaUpdate = Wrappers.lambdaUpdate();
        lambdaUpdate.eq(JobDO::getId,jobUpdateCO.getId())
                .eq(JobDO::getStatus, JobStatus.STOP.getCode());

        JobDO jobDO = BeanUtils.toBean(jobUpdateCO, JobDO::new);
        jobDO.setId(null);
        jobDO.setCreater(UserUtils.getUserName());
        jobDO.setStatus(JobStatus.STOP.getCode());
        jobDO.setNextTriggerTime(BeanUtils.toBean(jobDO, Job::new).getNextTriggerTime());

        int updateCount = jobMapper.update(jobDO, lambdaUpdate);
        if (updateCount <= 0) {
            return Result.handleFailure("无法更新运行中的任务");
        }
        return Result.ok("更新成功");
    }

    @Transactional
    @Override
    public Result<Void> removeJob(Long id) {
        jobMapper.deleteById(id);
        JobTriggerTimer.getInstance().remove(id);
        globalJobGroupHandler.remove(id);
        return Result.ok("删除成功");
    }

    @Override
    public Result<IPage<JobDTO>> pageJobs(JobQueryCO jobQueryCO) {
        LambdaQueryWrapper<JobDO> queryWrapper = Wrappers.<JobDO>lambdaQuery()
                .eq(jobQueryCO.getId() != null, JobDO::getId, jobQueryCO.getId())
                .like(jobQueryCO.getName() != null, JobDO::getName, jobQueryCO.getName())
                .eq(jobQueryCO.getWorkerId() != null, JobDO::getWorkerId, jobQueryCO.getWorkerId())
                .orderByDesc(JobDO::getCreateTime);
        IPage<JobDTO> jobPage = BeanUtils.toPageBean(jobMapper.selectPage(jobQueryCO.toPage(), queryWrapper), JobDTO::new);
        if (Utils.isNotEmpty(jobPage.getRecords())) {
            Set<Integer> set = jobPage.getRecords().stream().map(JobDTO::getWorkerId).collect(Collectors.toSet());
            Map<Integer, String> map = workerService.listByIds(set).stream().collect(Collectors.toMap(WorkerDTO::getId, WorkerDTO::getName));
            for (JobDTO jobDTO : jobPage.getRecords()) {
                jobDTO.setWorkerName(map.get(jobDTO.getWorkerId()));
            }
        }
        return Result.handleSuccess(jobPage);
    }

    @Transactional
    @Override
    public Result<Void> updateJobStatus(JobStatusUpdateCO jobStatusUpdateCO) {
        JobDO jobDO = jobMapper.selectById(jobStatusUpdateCO.getId());
        if (jobDO == null) {
            return Result.handleFailure("任务不存在");
        }
        // 执行更新
        LambdaUpdateWrapper<JobDO> lambdaUpdate = Wrappers.lambdaUpdate();
        lambdaUpdate.eq(JobDO::getId,jobStatusUpdateCO.getId())
                    .eq(Objects.equals(jobStatusUpdateCO.getStatus(),JobStatus.STOP.getCode()), JobDO::getStatus, JobStatus.RUN.getCode())
                    .eq(Objects.equals(jobStatusUpdateCO.getStatus(),JobStatus.RUN.getCode()),JobDO::getStatus, JobStatus.STOP.getCode())
                    .set(JobDO::getStatus,jobStatusUpdateCO.getStatus());
        int updateCount = jobMapper.update(null, lambdaUpdate);
        if (updateCount <= 0) {
            return Result.handleFailure("任务状态变更,请刷新重试");
        }
        // 取消任务执行
        if (Objects.equals(jobStatusUpdateCO.getStatus(),JobStatus.STOP.getCode())) {
            JobTriggerTimer.getInstance().remove(jobStatusUpdateCO.getId());
            return Result.ok();
        }

        // 将任务推入调度timer
        Job job = BeanUtils.toBean(jobDO, Job::new);
        Long triggerTime = job.getNextTriggerTime();
        if (triggerTime != null && (triggerTime - System.currentTimeMillis()) <= Commons.TIMER_MAX) {
            // JobTriggerTimer.getInstance().push(job);
            globalJobGroupHandler.pushIntoTimer(job);
        }
        return Result.ok();
    }

    @Override
    public Result<Void> run(JobRunCO jobRunCO) {
        JobDO jobDO = jobMapper.selectById(jobRunCO.getId());
        if (jobDO == null) {
            return Result.handleFailure("任务不存在");
        }
        Job job = BeanUtils.toBean(jobDO, Job::new);
        if (Objects.nonNull(jobRunCO.getJobParam())) {
            job.setParam(jobRunCO.getJobParam());
        }
        JobTriggerTimer.getInstance().directRun(job);
        return Result.ok("运行成功");
    }

    @Override
    public List<JobDO> listWaitingJobs(long maxTime) {
        Wrapper<JobDO> queryWrapper = Wrappers.<JobDO>lambdaQuery()
                .eq(JobDO::getStatus, JobStatus.RUN.getCode())
                .le(JobDO::getNextTriggerTime, maxTime);
        List<JobDO> jobList = new ArrayList<>();
        int page = 1;
        while (true) {
            Page<JobDO> jobPage = jobMapper.selectPage(new Page<>(page, 200), queryWrapper);
            jobList.addAll(jobPage.getRecords());
            if (!jobPage.hasNext()) {
                break;
            }
            page ++;
        }
        return jobList;
    }

    @Override
    public List<JobDO> listByIds(Set<Long> ids) {
        return jobMapper.selectBatchIds(ids);
    }

    @Override
    public void invoke(Job job) {
        List<String> workerAddressList = workerService.getWorkerAddressList(job.getWorkerId());
        RouteType routeType = RouteType.getInstance(job.getRouteType());
        Assert.notNull(routeType,"路由策略不支持");

        LocalDateTime now = LocalDateTimeUtil.now();
        // 保存调度日志
        DispatchLogDO dispatchLogDO = new DispatchLogDO();
        dispatchLogDO.setJobId(job.getId());
        dispatchLogDO.setWorkerId(job.getWorkerId());
        dispatchLogDO.setProcessorType(job.getProcessorType());
        dispatchLogDO.setProcessor(job.getProcessor());
        dispatchLogDO.setJobParam(job.getParam());
        dispatchLogDO.setTriggerTime(now);

        log.debug(">>>>>>>>>>>Trigger start, jobId:{}", job.getId());
        // 路由worker地址
        String workerAddress = routeType.getRouter().route(job, workerAddressList);
        if (workerAddress == null) {
            dispatchLogDO.setDispatchStatus(DispatchStatus.FAIL.getCode());
            dispatchLogDO.setErrorMsg("No available worker");
            dispatchLogMapper.insert(dispatchLogDO);
            return;
        }

        for (String address : workerAddress.split(",")) {
            DispatchLogDO dispatchLogInsert = BeanUtils.toBean(dispatchLogDO,DispatchLogDO::new);
            dispatchLogInsert.setWorkerAddress(address);
            dispatchLogMapper.insert(dispatchLogInsert);
            Assert.notNull(dispatchLogInsert.getId(),"保存调度记录错误");

            // 构建任务触发参数
            TaskTriggerParam taskTriggerParam = new TaskTriggerParam();
            taskTriggerParam.setJobId(job.getId());
            taskTriggerParam.setJobName(job.getProcessor());
            taskTriggerParam.setJobDispatchId(dispatchLogInsert.getId());
            taskTriggerParam.setJobParams(job.getParam());
            taskTriggerParam.setExecuteTimeout(job.getTimeoutS());
            taskTriggerParam.setTimeoutHandleType(job.getTimeoutHandleType());

            // 执行任务触发
            TaskResult taskResult = RpcUtils.toTaskResult(RpcUtils.call(address, BizCode.RUN, taskTriggerParam));
            DispatchLogDO dispatchLogUpdate = new DispatchLogDO()
                    .setId(taskTriggerParam.getJobDispatchId())
                    .setDispatchStatus(taskResult.isSuccess() ? DispatchStatus.OK.getCode(): DispatchStatus.FAIL.getCode())
                    .setExecuteStatus(taskResult.isSuccess() ? ExecuteStatus.ING.getCode() : null)
                    .setErrorMsg(taskResult.getMsg());
            dispatchLogMapper.updateById(dispatchLogUpdate);
        }

    }

    @Override
    public void refreshTriggerTime(Long jobId, Long lastTriggerTime, Long nextTriggerTime) {
        try {
            JobDO jobDO = new JobDO();
            jobDO.setId(jobId);
            jobDO.setLastTriggerTime(lastTriggerTime);
            jobDO.setNextTriggerTime(nextTriggerTime);
            jobMapper.updateById(jobDO);
        }catch (Exception e) {
            log.error("更新触发时间错误,{}",e.getMessage());
        }
    }

    @Transactional
    @Override
    public Result<Void> statusReport(StatusReportCO statusReportCO) {
        DispatchLogDO dispatchLogDO = new DispatchLogDO();
        dispatchLogDO.setId(statusReportCO.getDispatchId());
        dispatchLogDO.setExecuteStatus(statusReportCO.getExecuteStatus());
        dispatchLogDO.setExecuteStartTime(LocalDateTimeUtil.of(statusReportCO.getExecuteStartTime()));
        dispatchLogDO.setExecuteEndTime(LocalDateTimeUtil.of(statusReportCO.getExecuteEndTime()));
        dispatchLogMapper.updateById(dispatchLogDO);
        // TODO: 2023/6/1 延时任务处理
        return Result.ok("上报成功");
    }

    /**
     * 参数检查
     * @param jobUpdateCO
     */
    private void jobParamsCheck(JobUpdateCO jobUpdateCO) {
        // 生命周期参数验证
        if (jobUpdateCO.getStartTime() != null
                && jobUpdateCO.getStartTime().isBefore(LocalDateTimeUtil.now())) {
            throw new RuntimeException("开始时间不能在当前时间之前");
        }
        if (jobUpdateCO.getEndTime() != null
                && jobUpdateCO.getEndTime().isBefore(LocalDateTimeUtil.now())) {
            throw new RuntimeException("结束时间不能在当前时间之前");
        }
        if (jobUpdateCO.getEndTime() != null
                && jobUpdateCO.getStartTime() != null
                && jobUpdateCO.getEndTime().isBefore(jobUpdateCO.getStartTime())) {
            throw new RuntimeException("结束时间不能在开始时间之前");
        }

        // 处理器
        if (jobUpdateCO.getProcessorType() == 1 && jobUpdateCO.getProcessor() == null) {
            throw new RuntimeException("Bean模式须填写处理器");
        }
        // 触发器
        if (jobUpdateCO.getTriggerType() == TriggerType.CRON.getCode() && jobUpdateCO.getCron() == null) {
            throw new RuntimeException("cron触发须填写cron表达式");
        }
        if (jobUpdateCO.getTriggerType() == TriggerType.SPEED.getCode() && jobUpdateCO.getSpeedS() == null) {
            throw new RuntimeException("固定频率触发须填写频率");
        }
        if (jobUpdateCO.getTriggerType() == TriggerType.DELAYED.getCode() && jobUpdateCO.getDelayedS() == null) {
            throw new RuntimeException("固定延时触发须填写延时");
        }

    }

}
