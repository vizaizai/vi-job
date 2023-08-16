package com.github.vizaizai.server.timer;


import com.github.vizaizai.retry.timewheel.TimerTask;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.service.JobService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * 计时器任务
 * @author liaochongwei
 * @date 2023/8/11 14:26
 */
@Slf4j
public class InvokerTimerTask implements TimerTask {
    /**
     * 任务执行线程池
     */
    private final ExecutorService invokeExecutor;
    private final JobService jobService;
    private final Job job;
    /**
     * 直接运行
     */
    private boolean directRun = false;

    public InvokerTimerTask(ExecutorService invokeExecutor, JobService jobService, Job job) {
        this.invokeExecutor = invokeExecutor;
        this.jobService = jobService;
        this.job = job;
    }

    public InvokerTimerTask(ExecutorService invokeExecutor, JobService jobService, Job job, boolean directRun) {
        this.invokeExecutor = invokeExecutor;
        this.jobService = jobService;
        this.job = job;
        this.directRun = directRun;
    }

    @Override
    public void run() {
        if (directRun) {
            job.setDirectRun(true);
            this.directInvoke(job);
            return;
        }
        this.invoke(job);
    }

    /**
     * 直接执行
     * @param job 任务信息
     */
    private void directInvoke(Job job) {
        // 直接运行不更新触发时间
        invokeExecutor.execute(() -> {
            try {
                jobService.invoke(job);
            }catch (Exception e) {
                log.error("Job execute error.", e);
            }
        });
    }

    /**
     * 执行
     * @param job 任务信息
     */
    private void invoke(Job job) {
        if (job.isCanceled()) {
            log.info("任务-{}已取消",job.getId());
            return;
        }
        invokeExecutor.execute(() -> {
            try {
                // 触发调度
                jobService.invoke(job);
            } catch (Exception e) {
                log.error("Job execute error.", e);
            }finally {
                // 设置基准时间
                job.setBaseTime(job.getNextTriggerTime());
                // 重置触发时间
                job.resetNextTriggerTime();
                // 再次推入timer中
                JobTriggerTimer.getInstance().push(job);
                // 刷新触发时间
                jobService.refreshTriggerTime(job.getId(), job.getBaseTime(), job.getNextTriggerTime());
            }
        });
    }
}
