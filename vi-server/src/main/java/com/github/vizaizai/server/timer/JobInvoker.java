package com.github.vizaizai.server.timer;


import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.service.JobService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * 任务调用器
 * @author liaochongwei
 * @date 2023/8/11 14:26
 */
@Slf4j
public class JobInvoker {
    /**
     * 任务执行线程池
     */
    private final ExecutorService invokeExecutor;
    private final JobService jobService;
    private final Job job;

    public JobInvoker(ExecutorService invokeExecutor, JobService jobService, Job job) {
        this.invokeExecutor = invokeExecutor;
        this.jobService = jobService;
        this.job = job;
    }

    public void exec() {
        if (job.isDirectRun()) {
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
     * 执行任务触发
     * @param job 任务信息
     */
    private void invoke(Job job) {
        if (job.isCanceled()) {
            return;
        }

        invokeExecutor.execute(() -> {
            boolean refresh = false;
            try {
                // 设置基准时间
                job.setBaseTime(job.getNextTriggerTime());
                // 重置触发时间
                job.resetNextTriggerTime();
                // 刷新触发时间
                 refresh = jobService.refreshTriggerTime(job.getId(), job.getBaseTime(), job.getNextTriggerTime());
                // 推入timer
                if (refresh && job.prePush()) {
                    JobTriggerTimer.getInstance().push(job);
                }
                // 触发调度
                jobService.invoke(job);
            } catch (Exception e) {
                log.error("Job execute error.", e);
            }finally {
                if (refresh && job.postPush()) {
                    JobTriggerTimer.getInstance().push(job);
                }
            }
        });
    }
}
