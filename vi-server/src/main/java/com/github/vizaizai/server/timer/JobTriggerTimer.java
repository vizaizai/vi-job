package com.github.vizaizai.server.timer;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.github.vizaizai.retry.timewheel.HashedWheelTimer;
import com.github.vizaizai.retry.timewheel.Timeout;
import com.github.vizaizai.retry.timewheel.TimerTask;
import com.github.vizaizai.server.constant.Commons;
import com.github.vizaizai.server.constant.TriggerType;
import com.github.vizaizai.server.entity.Job;
import com.github.vizaizai.server.service.JobService;
import com.github.vizaizai.server.timer.watch.WatchDogRunner;
import com.github.vizaizai.server.timer.watch.WatchInstance;
import com.github.vizaizai.server.utils.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.system.SystemProperties;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 任务触发器Timer
 * @author liaochongwei
 * @date 2023/5/18 20:50
 */
@Slf4j
public class JobTriggerTimer {

    /**
     * 任务执行线程池
     */
    private final ExecutorService invokeExecutor;
    /**
     * 慢时间轮
     */
    private final HashedWheelTimer slowHashedWheelTimer;
    /***
     * 快时间轮
     */
    private final HashedWheelTimer fastHashedWheelTimer;

    private final Map<Long,Timeout> timeouts = new ConcurrentHashMap<>();
    /**
     * 任务信息
     */
    private final Map<Long, Job> jobs = new ConcurrentHashMap<>();
    private static JobService jobService  = null;

    private JobTriggerTimer() {
        slowHashedWheelTimer = new HashedWheelTimer(500L, 1024, 0, 1,-1);
        fastHashedWheelTimer = new HashedWheelTimer(10L, 4096, 1, Runtime.getRuntime().availableProcessors() * 2, -1);

        String size = SystemProperties.get("job.trigger.maxPoolSize");
        int poolSize = StringUtils.isNotBlank(size) ? Integer.parseInt(size) : 200;
        invokeExecutor = new ThreadPoolExecutor(
                10,
                poolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000),
                new BasicThreadFactory.Builder().namingPattern("Trigger-task-%d").build());

        log.info(">>>>>>>>>>Trigger timer started");

    }

    private static final class JobTriggerTimerHolder {
        static final JobTriggerTimer jobTriggerTimer = new JobTriggerTimer();
    }

    public static synchronized JobTriggerTimer getInstance() {
        if (jobService == null) {
            jobService = ContextUtil.getBean(JobService.class);
        }
        return JobTriggerTimerHolder.jobTriggerTimer;
    }


    /**
     * 将job推入到Timer
     * @param job 任务实体
     */
    public synchronized void push(Job job) {
        // 任务重复调度
        if (getTimeout(job.getId()) != null) {
            return;
        }
        // 延时任务校验，保证上一次调度执行结束
        if (Objects.equals(job.getTriggerType(), TriggerType.DELAYED.getCode())) {
            if (WatchDogRunner.getInstance().isRunning(WatchInstance.getWatchId1(job.getId()))) {
                return;
            }
        }

        // 触发时间检查
        Long triggerTime = job.getNextTriggerTime();
        if (triggerTime == null) {
            return;
        }

        // 生命周期检查
        if (job.getEndTime() != null
                && System.currentTimeMillis() > LocalDateTimeUtil.toEpochMilli(job.getEndTime())) {
            invokeExecutor.execute(()-> jobService.stop(job.getId()));
            remove(job.getId());
            return;
        }
        // 延时毫秒数
        long delay = triggerTime - System.currentTimeMillis();
        if (delay > Commons.TIMER_MAX) {
            return;
        }
        jobs.put(job.getId(), job);

        // 任务过期，计算下次调度时间
        if (delay < 0) {
            job.setBaseTime(System.currentTimeMillis());
            job.resetNextTriggerTime();
            this.push(job);
            return;
        }

        // 延时调度
        this.schedule(delay, job.getId(), new InvokerTimerTask(invokeExecutor, jobService, job));

    }


    /**
     * 定时任务
     * @param delay 延时毫秒数
     * @param jobId 任务id
     * @
     */
    private void schedule(long delay, long jobId, TimerTask timerTask) {
        // 延时时间大于最小时间->推入慢时间轮循环
        if (delay > Commons.TIMER_MIN) {
            Timeout timeout = slowHashedWheelTimer.newTimeout(()-> {
                timeouts.remove(jobId);
                // 再次schedule，进入快时间轮
                this.schedule(Commons.TIMER_MIN, jobId, timerTask);
            }
            ,delay - Commons.TIMER_MIN
            ,TimeUnit.MILLISECONDS);
            // 将Timeout记录，用于中止任务
            timeouts.put(jobId, timeout);
        }else if (delay > 50L) {
            Timeout timeout = fastHashedWheelTimer.newTimeout(()->{
                timeouts.remove(jobId);
                // 运行任务
                timerTask.run();
            }
            ,delay
            ,TimeUnit.MILLISECONDS);
            timeouts.put(jobId, timeout);
        }else  {
            // 运行任务
            timerTask.run();
        }
    }
    /**
     * 从Timer中移除job
     * @param jobId 任务id
     */
    public synchronized void remove(Long jobId) {
        Timeout timeout;
        timeout = timeouts.remove(jobId);
        if (timeout != null) {
            timeout.cancel();
        }
        Job job = jobs.remove(jobId);
        if (job != null) {
            job.setCanceled(true);
        }
    }

    /**
     * 直接运行
     * @param job 任务
     */
    public void directRun(Job job) {
        new InvokerTimerTask(invokeExecutor, jobService, job, true).run();
    }

    public HashedWheelTimer getSlowHashedWheelTimer() {
        return slowHashedWheelTimer;
    }

    public HashedWheelTimer getFastHashedWheelTimer() {
        return fastHashedWheelTimer;
    }

    public Timeout getTimeout(Long jobId) {
        return timeouts.get(jobId);
    }

}
