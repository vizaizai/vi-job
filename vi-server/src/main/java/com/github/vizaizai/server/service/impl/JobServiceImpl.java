package com.github.vizaizai.server.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.vizaizai.common.contants.BizCode;
import com.github.vizaizai.common.contants.ExecuteStatus;
import com.github.vizaizai.common.model.*;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.retry.util.Assert;
import com.github.vizaizai.server.config.ServerProperties;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.constant.DispatchStatus;
import com.github.vizaizai.server.constant.JobStatus;
import com.github.vizaizai.server.constant.TriggerType;
import com.github.vizaizai.server.dao.JobMapper;
import com.github.vizaizai.server.dao.dataobject.JobDO;
import com.github.vizaizai.server.dao.dataobject.JobInstanceDO;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.entity.JobCache;
import com.github.vizaizai.server.raft.kv.Type;
import com.github.vizaizai.server.router.RouteType;
import com.github.vizaizai.server.service.GlobalJobGroupManager;
import com.github.vizaizai.server.service.JobInstanceService;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.service.WorkerService;
import com.github.vizaizai.server.timer.watch.ExecPredicate;
import com.github.vizaizai.server.timer.watch.WatchDogRunner;
import com.github.vizaizai.server.timer.watch.WatchInstance;
import com.github.vizaizai.server.utils.BeanUtils;
import com.github.vizaizai.server.utils.KVUtils;
import com.github.vizaizai.server.utils.RpcUtils;
import com.github.vizaizai.server.utils.UserUtils;
import com.github.vizaizai.server.web.co.*;
import com.github.vizaizai.server.web.dto.JobDTO;
import com.github.vizaizai.server.web.dto.JobRunDTO;
import com.github.vizaizai.server.web.dto.PingResult;
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
    private JobInstanceService jobInstanceService;
    @Resource
    private WorkerService workerService;
    @Resource
    private GlobalJobGroupManager globalJobGroupHandler;
    @Resource
    private ServerProperties serverProperties;

    @Override
    public Result<Void> addJob(JobUpdateCO jobUpdateCO) {
        this.jobParamsCheck(jobUpdateCO);
        JobDO jobDO = BeanUtils.toBean(jobUpdateCO, JobDO::new);
        jobDO.setCreater(UserUtils.getUserName());
        jobDO.setStatus(JobStatus.STOP.getCode());
        jobDO.setNextTriggerTime(BeanUtils.toBean(jobDO, Job::new).initNextTriggerTime());
        jobMapper.insert(jobDO);
        // 全局任务分组
        globalJobGroupHandler.assign(jobDO.getId());
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
        jobDO.setUpdater(UserUtils.getUserName());
        jobDO.setStatus(JobStatus.STOP.getCode());
        jobDO.setNextTriggerTime(BeanUtils.toBean(jobDO, Job::new).initNextTriggerTime());

        int updateCount = jobMapper.update(jobDO, lambdaUpdate);
        if (updateCount <= 0) {
            return Result.handleFailure("无法更新运行中的任务");
        }
        KVUtils.removeKey(Commons.JOB_KEY + jobUpdateCO.getId(), Type.STRING);
        return Result.ok("更新成功");
    }

    @Transactional
    @Override
    public Result<Void> removeJob(Long id) {
        jobMapper.deleteById(id);
        globalJobGroupHandler.remove(id);
        KVUtils.removeKey(Commons.JOB_KEY + id, Type.STRING);
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
        // 判断生命周期结束
        if (Objects.equals(jobStatusUpdateCO.getStatus(),JobStatus.RUN.getCode())
                && jobDO.getEndTime() != null
                && System.currentTimeMillis() > LocalDateTimeUtil.toEpochMilli(jobDO.getEndTime())) {
            return Result.handleFailure("任务生命周期已结束，无法开启");
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
        // 停止->取消任务执行
        if (Objects.equals(jobStatusUpdateCO.getStatus(),JobStatus.STOP.getCode())) {
            globalJobGroupHandler.removeFormTimer(jobStatusUpdateCO.getId());
            return Result.ok();
        }

        // 启动->将任务推入调度timer
        Job job = BeanUtils.toBean(jobDO, Job::new);
        globalJobGroupHandler.pushIntoTimer(job);
        return Result.ok();
    }

    @Transactional
    @Override
    public Result<Void> statusReport(List<StatusReportCO> statusReportList) {
        if (Utils.isEmpty(statusReportList)) {
            return Result.ok();
        }

        List<JobInstanceDO> jobInstances = new ArrayList<>();
        for (StatusReportCO statusReportCO : statusReportList) {
            JobInstanceDO jobInstanceDO = new JobInstanceDO();
            jobInstanceDO.setId(statusReportCO.getJobInstanceId());
            jobInstanceDO.setExecuteStatus(statusReportCO.getExecuteStatus());
            jobInstanceDO.setExecuteStartTime(LocalDateTimeUtil.of(statusReportCO.getExecuteStartTime()));
            jobInstanceDO.setExecuteEndTime(LocalDateTimeUtil.of(statusReportCO.getExecuteEndTime()));
            jobInstanceDO.setExecCount(statusReportCO.getExecCount());
            jobInstances.add(jobInstanceDO);

            // 延时任务：结束监听
            if (Objects.equals(statusReportCO.getTriggerType(), TriggerType.DELAYED.getCode())) {
                globalJobGroupHandler.endWatchForJobExec(statusReportCO.getJobId());
            }
        }
        jobInstanceService.updateBatchById(jobInstances);


        return Result.ok("上报成功");
    }

    @Transactional
    @Override
    public Result<JobRunDTO> run(JobRunCO jobRunCO) {
        long triggerTime = LocalDateTimeUtil.toEpochMilli(jobRunCO.getTriggerTime());
        JobRunParam jobRunParam = new JobRunParam();
        jobRunParam.setId(jobRunCO.getId());
        jobRunParam.setJobCode(jobRunCO.getJobCode());
        jobRunParam.setJobParam(jobRunCO.getJobParam());
        jobRunParam.setTriggerTime(triggerTime);

        Result<Long> runResult = this.run(jobRunParam);
        if (Objects.equals(runResult.getCode(), StatusCode.SUCCESS.getCode())
                && runResult.getData() != null) {
            JobRunDTO jobRunDTO = new JobRunDTO();
            jobRunDTO.setJobId(runResult.getData());
            if (Utils.isNotBlank(jobRunCO.getAddress())) {
                PingResult ping = workerService.ping(jobRunCO.getAddress());
                if (ping.getSuccess()) {
                    jobRunDTO.setOriginId(ping.getOriginId());
                }
            }
            return Result.handleSuccess(jobRunDTO);
        }
        return Result.handleFailure(runResult.getMsg());
    }

    @Override
    public Result<Long> run(JobRunParam jobRunParam) {
        log.info(">>>>>>>>>run: {}", JSONUtil.toJsonStr(jobRunParam));
        JobCache jobCache;
        if (jobRunParam.getId() != null && jobRunParam.getJobCode() != null) {
            jobCache = this.getJobCache(jobRunParam.getId());
            if (jobCache != null && !jobRunParam.getJobCode().equals(jobCache.getCode())) {
                return Result.handleFailure("Job is not exists");
            }
        }else if (jobRunParam.getId() != null) {
            jobCache = this.getJobCache(jobRunParam.getId());
        }else if (jobRunParam.getJobCode() != null) {
            jobCache = BeanUtils.toBean(jobMapper.selectOne(Wrappers.<JobDO>lambdaQuery().eq(JobDO::getCode, jobRunParam.getJobCode())), JobCache::new);
        }else {
            return Result.handleFailure("The job identify[id, code] must be not null");
        }
        if (jobCache == null) {
            return Result.handleFailure("Job is not exists");
        }
        Job job = BeanUtils.toBean(jobCache, Job::new);
        if (Objects.nonNull(jobRunParam.getJobParam())) {
            job.setParam(jobRunParam.getJobParam());
        }
        long triggerTime = jobRunParam.getTriggerTime();
        // 预生成任务实例
        JobInstanceDO jobInstanceDO = this.createBaseInstance(job);
        jobInstanceDO.setTriggerTime(LocalDateTimeUtil.of(triggerTime));
        jobInstanceService.saveOrUpdate(jobInstanceDO);
        job.setDirectRun(true);

        job.setNextTriggerTime(triggerTime);
        job.setInstanceId(jobInstanceDO.getId());

        if (triggerTime - System.currentTimeMillis() <= Commons.TIMER_MAX) {
            globalJobGroupHandler.pushIntoTimer(job);
        }
        return Result.handleSuccess(job.getId());
    }

    @Override
    public List<JobDO> listWaitingJobs(long maxTime) {
        Wrapper<JobDO> queryWrapper = Wrappers.<JobDO>lambdaQuery()
                .eq(JobDO::getStatus, JobStatus.RUN.getCode())
                .le(JobDO::getNextTriggerTime, maxTime)
                .orderByAsc(JobDO::getNextTriggerTime);
        Page<JobDO> jobPage = jobMapper.selectPage(new Page<>(1, serverProperties.getTriggerMaximum()), queryWrapper);
        return jobPage.getRecords();
    }

    @Override
    public void invoke(Job job) {
        List<String> workerAddressList = workerService.getWorkerAddressList(job.getWorkerId());
        RouteType routeType = RouteType.getInstance(job.getRouteType());
        Assert.notNull(routeType,"路由策略不支持");
        // 保存任务实例
        JobInstanceDO jobInstanceDO = this.createBaseInstance(job);

        log.info(">>>>>>>>>>>Trigger start, jobId:{}", job.getId());
        // 路由worker地址
        String workerAddress = routeType.getRouter().route(job, workerAddressList);
        if (Utils.isBlank(workerAddress)) {
            jobInstanceDO.setDispatchStatus(DispatchStatus.FAIL.getCode());
            jobInstanceDO.setErrorMsg("No available worker");
            jobInstanceService.saveOrUpdate(jobInstanceDO);
            return;
        }


        for (String address : workerAddress.split(",")) {
            JobInstanceDO jobInstanceUpdate = BeanUtils.toBean(jobInstanceDO, JobInstanceDO::new);
            jobInstanceUpdate.setWorkerAddress(address);
            jobInstanceService.saveOrUpdate(jobInstanceUpdate);
            Assert.notNull(jobInstanceUpdate.getId(),"保存任务实例错误");
            // 重置父id
            if (jobInstanceDO.getPid() == 0L) {
                jobInstanceDO.setPid(jobInstanceUpdate.getId());
                jobInstanceDO.setId(null);
            }
            // 构建任务触发参数
            TaskTriggerParam taskTriggerParam = new TaskTriggerParam();
            taskTriggerParam.setJobId(job.getId());
            taskTriggerParam.setJobName(job.getProcessor());
            taskTriggerParam.setJobInstanceId(jobInstanceUpdate.getId());
            taskTriggerParam.setJobParams(job.getParam());
            taskTriggerParam.setTriggerType(job.getTriggerType());
            taskTriggerParam.setTriggerTime(LocalDateTimeUtil.toEpochMilli(jobInstanceDO.getTriggerTime()));
            taskTriggerParam.setExecuteTimeout(job.getTimeoutS());
            taskTriggerParam.setMaxWaitNum(job.getMaxWaitNum());
            taskTriggerParam.setRetryCount(job.getRetryCount());

            // 执行任务触发
            TaskResult taskResult = RpcUtils.toTaskResult(RpcUtils.call(address, BizCode.EXEC, taskTriggerParam));
            jobInstanceUpdate = new JobInstanceDO()
                    .setId(taskTriggerParam.getJobInstanceId())
                    .setDispatchStatus(taskResult.isSuccess() ? DispatchStatus.OK.getCode(): DispatchStatus.FAIL.getCode())
                    .setExecuteStatus(taskResult.isSuccess() ? ExecuteStatus.ING.getCode() : null)
                    .setErrorMsg(taskResult.getMsg());
            jobInstanceService.saveOrUpdate(jobInstanceUpdate);

            // 延时任务启动监听
            if (!job.isDirectRun()
                    && Objects.equals(job.getTriggerType(), TriggerType.DELAYED.getCode())) {
                WatchInstance watchInstance = new WatchInstance();
                watchInstance.setWatchId(WatchInstance.getWatchId1(taskTriggerParam.getJobId()));
                Map<String, Object> extras = new HashMap<>();
                extras.put("jobId",taskTriggerParam.getJobId());
                extras.put("jobInstanceId",taskTriggerParam.getJobInstanceId());
                extras.put("workerAddr",address);
                watchInstance.setExtras(extras);
                watchInstance.setWatchPredicate(new ExecPredicate());
                watchInstance.setCompleter((e)-> {
                    JobDO jobDO = jobMapper.selectById((long) e.getExtras().get("jobId"));
                    if (jobDO != null && Objects.equals(jobDO.getStatus(), JobStatus.RUN.getCode())) {
                        globalJobGroupHandler.pushIntoTimer(BeanUtils.toBean(jobDO, Job::new).resetNextTriggerTime());
                    }
                });
                WatchDogRunner.getInstance().start(watchInstance);
            }
        }


    }

    @Override
    public boolean refreshTriggerTime(Long jobId, Long lastTriggerTime, Long nextTriggerTime) {
        try {
            LambdaUpdateWrapper<JobDO> updateWrapper = Wrappers.<JobDO>lambdaUpdate()
                    .eq(JobDO::getId, jobId)
                    .eq(JobDO::getStatus, JobStatus.RUN.getCode());
            JobDO jobDO = new JobDO();
            jobDO.setLastTriggerTime(lastTriggerTime);
            jobDO.setNextTriggerTime(nextTriggerTime);
            return jobMapper.update(jobDO,updateWrapper) > 0;
        }catch (Exception e) {
            log.error("Trigger time refresh fail,{}",e.getMessage());
            return false;
        }
    }

    @Transactional
    @Override
    public void stop(Long jobId) {
        JobDO jobDO = new JobDO();
        jobDO.setId(jobId);
        jobDO.setStatus(JobStatus.STOP.getCode());
        jobMapper.updateById(jobDO);
    }

    @Override
    public List<JobDO> listByIds(List<Long> ids) {
        return jobMapper.selectBatchIds(ids);
    }

    @Override
    public JobCache getJobCache(Long id) {
        String value = KVUtils.get(Commons.JOB_KEY + id);
        JobCache jobCache;
        if (value == null) {
            JobDO jobDO = jobMapper.selectById(id);
            if (jobDO == null) {
                return null;
            }
            jobCache = BeanUtils.toBean(jobDO, JobCache::new);
            KVUtils.set(Commons.JOB_KEY + id, JSONUtil.toJsonStr(jobCache));
            return jobCache;
        }
        return JSONUtil.parseObj(value).toBean(JobCache.class);
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

    /**
     * 创建基础任务实例
     * @param job 任务实体
     * @return JobInstanceDO
     */
   private JobInstanceDO createBaseInstance(Job job) {
        LocalDateTime now = LocalDateTimeUtil.now();
       JobInstanceDO jobInstanceDO = new JobInstanceDO();
       jobInstanceDO.setId(job.getInstanceId());
       jobInstanceDO.setPid(0L);
       jobInstanceDO.setJobId(job.getId());
       jobInstanceDO.setWorkerId(job.getWorkerId());
       jobInstanceDO.setProcessorType(job.getProcessorType());
       jobInstanceDO.setProcessor(job.getProcessor());
       jobInstanceDO.setJobParam(job.getParam());
       jobInstanceDO.setTriggerTime(now);
       jobInstanceDO.setDispatchStatus(DispatchStatus.WAIT.getCode());
       if (job.getLogAutoDelHours() != null) {
           jobInstanceDO.setExpectedDeleteTime(now.plusHours(job.getLogAutoDelHours()));
       }
       return jobInstanceDO;
   }

}
