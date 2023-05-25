package com.github.vizaizai.server.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.common.model.TaskContext;
import com.github.vizaizai.common.model.TaskResult;
import com.github.vizaizai.remote.common.BizCode;
import com.github.vizaizai.retry.timewheel.Timeout;
import com.github.vizaizai.retry.util.Assert;
import com.github.vizaizai.server.constant.DispatchStatus;
import com.github.vizaizai.server.constant.ExecuteStatus;
import com.github.vizaizai.server.constant.JobStatus;
import com.github.vizaizai.server.constant.TriggerType;
import com.github.vizaizai.server.dao.DispatchLogMapper;
import com.github.vizaizai.server.dao.JobMapper;
import com.github.vizaizai.server.dao.dataobject.DispatchLogDO;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.router.RouteType;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.timer.JobTriggerTimer;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.utils.RpcUtils;
import com.github.vizaizai.server.utils.UserUtils;
import com.github.vizaizai.server.web.co.JobStatusUpdateCO;
import com.github.vizaizai.server.web.co.JobUpdateCO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public Result<Void> addJob(JobUpdateCO jobUpdateCO) {
        this.jobParamsCheck(jobUpdateCO);
        jobUpdateCO.setCreater(UserUtils.getUserName());
        JobDO jobDO = BeanUtils.toBean(jobUpdateCO, JobDO::new);
        jobDO.setStatus(JobStatus.STOP.getCode());
        jobMapper.insert(jobDO);
        return Result.ok("新增成功");
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
            Timeout timeout = JobTriggerTimer.getInstance().getTimeout(jobStatusUpdateCO.getId());
            if (timeout != null) {
                timeout.cancel();
            }
            return Result.ok();
        }

        // 将任务推入调度timer
        Job job = BeanUtils.toBean(jobDO, Job::new);
        job.setWorkerAddressList(workerService.getWorkerAddressList(job.getWorkerId()));
        JobTriggerTimer.getInstance().pushTimer(job);

        return Result.ok();
    }

    @Override
    public void invoke(Job job) {
        RouteType routeType = RouteType.getInstance(job.getRouteType());
        Assert.notNull(routeType,"路由策略不支持");

        LocalDateTime now = LocalDateTimeUtil.now();
        // 保存调度日志
        DispatchLogDO dispatchLogDO = new DispatchLogDO();
        dispatchLogDO.setJobId(job.getId());
        dispatchLogDO.setWorkerId(job.getWorkerId());
        dispatchLogDO.setDispatchStatus(DispatchStatus.OK.getCode());
        dispatchLogDO.setProcessorType(job.getProcessorType());
        dispatchLogDO.setProcessor(job.getProcessor());
        dispatchLogDO.setJobParam(job.getParam());
        dispatchLogDO.setTriggerTime(now);

        // 路由worker地址
        String workerAddress = routeType.getRouter().route(job, job.getWorkerAddressList());
        if (workerAddress == null) {
            dispatchLogDO.setDispatchStatus(DispatchStatus.FAIL.getCode());
            dispatchLogDO.setErrorMsg("No available worker");
            dispatchLogMapper.insert(dispatchLogDO);
            return;
        }
        log.debug(">>>>>>>>>>>Trigger start, jobId:{}", job.getId());
        dispatchLogDO.setExecuteStatus(ExecuteStatus.ING.getCode());

        for (String address : workerAddress.split(",")) {

            DispatchLogDO dispatchLogTmp = BeanUtils.toBean(dispatchLogDO,DispatchLogDO::new);
            dispatchLogTmp.setWorkerAddress(address);
            dispatchLogMapper.insert(dispatchLogDO);
            Assert.notNull(dispatchLogDO.getId(),"保存调度记录错误");

            // 构建任务上下文
            TaskContext taskContext = new TaskContext();
            taskContext.setJobId(job.getId());
            taskContext.setJobName(job.getProcessor());
            taskContext.setJobDispatchId(dispatchLogDO.getId());
            taskContext.setJobParams(job.getParam());
            taskContext.setExecuteTimeout(job.getTimeoutS());
            taskContext.setTimeoutHandleType(job.getTimeoutHandleType());

            // 执行任务触发
            TaskResult taskResult = RpcUtils.toTaskResult(RpcUtils.call(address, BizCode.RUN, taskContext));
            // 调度失败->更新调度日志
            if (!taskResult.isSuccess()) {
                DispatchLogDO dispatchLogUpdate = new DispatchLogDO()
                        .setId(taskContext.getJobDispatchId())
                        .setDispatchStatus(DispatchStatus.FAIL.getCode())
                        .setExecuteStatus(ExecuteStatus.FAIL.getCode())
                        .setErrorMsg(taskResult.getMsg());
                dispatchLogMapper.updateById(dispatchLogUpdate);
            }
        }

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
